import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.*;

/**
 * Gera o diagrama de classes (Graphviz .dot) lendo as classes compiladas por reflexão,
 * para o diagrama sempre bater com o código.
 *
 * Uso (na raiz do projeto, depois de ./mvnw package):
 *   java -cp target/bairro-ativo.jar docs/diagramas/GeradorUml.java
 *   dot -Tpng docs/diagramas/diagrama-classes.dot -o docs/diagramas/diagrama-classes.png
 */
public class GeradorUml {

    static final String BASE = "br.com.fiap.bairroativo";
    static final int POR_LINHA = 4;

    public static void main(String[] args) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        try (JarFile jar = new JarFile("target/bairro-ativo.jar")) {
            for (var e : Collections.list(jar.entries())) {
                String n = e.getName();
                if (n.startsWith(BASE.replace('.', '/')) && n.endsWith(".class") && !n.contains("$")) {
                    classes.add(Class.forName(n.replace('/', '.').replace(".class", "")));
                }
            }
        }
        classes.sort(Comparator.comparing(Class::getName));
        gerar(classes, "docs/diagramas/diagrama-classes.dot", "Bairro Ativo - Diagrama de classes (completo)");
        gerar(classes.stream().filter(c -> pacote(c).equals("model")).toList(),
                "docs/diagramas/diagrama-classes-dominio.dot", "Bairro Ativo - Diagrama de classes (domínio / model)");
    }

    static String pacote(Class<?> c) {
        String p = c.getPackageName();
        return p.equals(BASE) ? "(raiz)" : p.substring(BASE.length() + 1);
    }

    static void gerar(List<Class<?>> classes, String arquivo, String titulo) throws Exception {
        Set<Class<?>> conjunto = new HashSet<>(classes);
        StringBuilder sb = new StringBuilder();
        sb.append("digraph UML {\n  graph [label=\"").append(titulo).append("\", labelloc=t, fontsize=22, fontname=\"Helvetica\", rankdir=TB, nodesep=0.35, ranksep=0.7, newrank=true];\n");
        sb.append("  node [shape=plain, fontname=\"Helvetica\", fontsize=10];\n  edge [fontname=\"Helvetica\", fontsize=9];\n");

        Map<String, List<Class<?>>> porPacote = classes.stream()
                .collect(Collectors.groupingBy(GeradorUml::pacote, TreeMap::new, Collectors.toList()));
        for (var e : porPacote.entrySet()) {
            sb.append("  subgraph \"cluster_").append(e.getKey()).append("\" {\n    label=\"pacote ").append(e.getKey())
              .append("\"; style=\"rounded,dashed\"; color=\"#9aa5a0\"; fontsize=13;\n");
            for (Class<?> c : e.getValue()) sb.append("    ").append(no(c)).append("\n");
            // quebra pacotes grandes em linhas para o diagrama não ficar largo demais
            List<Class<?>> lista = e.getValue();
            for (int i = 0; i + POR_LINHA < lista.size(); i++) {
                sb.append("    ").append(id(lista.get(i))).append(" -> ").append(id(lista.get(i + POR_LINHA)))
                  .append(" [style=invis, weight=10];\n");
            }
            sb.append("  }\n");
        }

        Set<String> arestas = new LinkedHashSet<>();
        for (Class<?> c : classes) {
            if (c.getSuperclass() != null && conjunto.contains(c.getSuperclass())) {
                arestas.add(id(c) + " -> " + id(c.getSuperclass()) + " [arrowhead=empty, color=\"#0f5c4a\", penwidth=1.4];");
            }
            for (Class<?> i : c.getInterfaces()) {
                if (conjunto.contains(i)) arestas.add(id(c) + " -> " + id(i) + " [arrowhead=empty, style=dashed, color=\"#0f5c4a\"];");
            }
            for (Field f : campos(c)) {
                Class<?> alvo = alvo(f.getGenericType());
                if (alvo != null && conjunto.contains(alvo) && alvo != c && !alvo.isEnum()) {
                    String mult = Collection.class.isAssignableFrom(f.getType()) ? "0..*" : "1";
                    arestas.add(id(c) + " -> " + id(alvo) + " [arrowhead=vee, headlabel=\"" + mult + "\", color=\"#45524d\"];");
                } else if (alvo != null && conjunto.contains(alvo) && alvo.isEnum()) {
                    arestas.add(id(c) + " -> " + id(alvo) + " [arrowhead=vee, style=dotted, color=\"#8a96a0\"];");
                }
            }
        }
        arestas.forEach(a -> sb.append("  ").append(a).append("\n"));
        sb.append("}\n");
        Files.writeString(Path.of(arquivo), sb.toString());
        System.out.println(arquivo + ": " + classes.size() + " classes, " + arestas.size() + " relacionamentos");
    }

    static String id(Class<?> c) { return "\"" + c.getSimpleName() + "\""; }

    static List<Field> campos(Class<?> c) {
        if (c.isRecord()) {
            return Arrays.stream(c.getRecordComponents()).map(rc -> {
                try { return c.getDeclaredField(rc.getName()); } catch (NoSuchFieldException e) { throw new RuntimeException(e); }
            }).toList();
        }
        return Arrays.stream(c.getDeclaredFields())
                .filter(f -> !f.isSynthetic() && !f.isEnumConstant() && !f.getName().startsWith("$"))
                .toList();
    }

    static Class<?> alvo(Type t) {
        if (t instanceof Class<?> c) return c;
        if (t instanceof ParameterizedType p) {
            Type[] a = p.getActualTypeArguments();
            Type ultimo = a[a.length - 1];
            if (ultimo instanceof Class<?> c) return c;
        }
        return null;
    }

    static String no(Class<?> c) {
        String estereotipo = c.isInterface() ? "«interface»" : c.isEnum() ? "«enumeration»" : c.isRecord() ? "«record»"
                : Modifier.isAbstract(c.getModifiers()) ? "«abstract»" : "";
        String cor = switch (pacote(c)) {
            case "model" -> "#e3f1ec"; case "dao" -> "#e5edff"; case "service" -> "#fdf1de";
            case "resource" -> "#f0e8fa"; case "dto" -> "#f1f4f2"; case "exception" -> "#fde8e6"; default -> "#ffffff";
        };
        StringBuilder t = new StringBuilder("<table border=\"1\" cellborder=\"0\" cellspacing=\"0\" cellpadding=\"3\" bgcolor=\"white\" color=\"#45524d\">");
        t.append("<tr><td bgcolor=\"").append(cor).append("\">");
        if (!estereotipo.isEmpty()) t.append("<font point-size=\"9\">").append(html(estereotipo)).append("</font><br/>");
        String nome = Modifier.isAbstract(c.getModifiers()) && !c.isInterface() ? "<i>" + c.getSimpleName() + "</i>" : c.getSimpleName();
        t.append("<b>").append(nome).append("</b></td></tr>");

        List<String> atributos = new ArrayList<>();
        if (c.isEnum()) {
            for (Object k : c.getEnumConstants()) atributos.add(((Enum<?>) k).name());
        }
        for (Field f : campos(c)) {
            String linha = vis(f.getModifiers(), c.isRecord()) + " " + f.getName() + " : " + tipo(f.getGenericType());
            if (Modifier.isStatic(f.getModifiers())) linha = "<u>" + html(linha) + "</u>"; else linha = html(linha);
            atributos.add(linha);
        }
        t.append(secao(atributos, false));

        List<String> metodos = new ArrayList<>();
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            if (k.isSynthetic() || Modifier.isPrivate(k.getModifiers()) || c.isEnum()) continue;
            if (c.isRecord()) continue;
            metodos.add(html(vis(k.getModifiers(), false) + " " + c.getSimpleName() + "(" + params(k.getGenericParameterTypes(), k.getParameters()) + ")"));
        }
        Set<String> componentes = c.isRecord()
                ? Arrays.stream(c.getRecordComponents()).map(RecordComponent::getName).collect(Collectors.toSet()) : Set.of();
        Method[] ms = c.getDeclaredMethods();
        Arrays.sort(ms, Comparator.comparing(Method::getName).thenComparing(m -> m.getParameterCount()));
        for (Method m : ms) {
            if (m.isSynthetic() || m.isBridge() || m.getName().startsWith("lambda$")) continue;
            if (c.isEnum() && (m.getName().equals("values") || m.getName().equals("valueOf"))) continue;
            if (c.isRecord() && (componentes.contains(m.getName()) || Set.of("equals", "hashCode", "toString").contains(m.getName()))) continue;
            String linha = vis(m.getModifiers(), false) + " " + m.getName() + "(" + params(m.getGenericParameterTypes(), m.getParameters()) + ") : " + tipo(m.getGenericReturnType());
            linha = html(linha);
            if (Modifier.isStatic(m.getModifiers())) linha = "<u>" + linha + "</u>";
            if (Modifier.isAbstract(m.getModifiers()) && !c.isInterface()) linha = "<i>" + linha + "</i>";
            metodos.add(linha);
        }
        t.append(secao(metodos, true));
        t.append("</table>");
        return id(c) + " [label=<" + t + ">];";
    }

    static String secao(List<String> linhas, boolean ultima) {
        if (linhas.isEmpty()) return "<hr/><tr><td> </td></tr>";
        return "<hr/><tr><td align=\"left\" balign=\"left\">" + String.join("<br align=\"left\"/>", linhas) + "<br align=\"left\"/></td></tr>";
    }

    static String params(Type[] tipos, Parameter[] ps) {
        List<String> r = new ArrayList<>();
        for (int i = 0; i < tipos.length; i++) {
            String nome = ps[i].isNamePresent() ? ps[i].getName() : "p" + i;
            r.add(nome + " : " + tipo(tipos[i]));
        }
        return String.join(", ", r);
    }

    static String vis(int mod, boolean registro) {
        if (registro) return "-";
        if (Modifier.isPublic(mod)) return "+";
        if (Modifier.isProtected(mod)) return "#";
        if (Modifier.isPrivate(mod)) return "-";
        return "~";
    }

    static String tipo(Type t) {
        if (t instanceof Class<?> c) return c.isArray() ? tipo(c.getComponentType()) + "[]" : c.getSimpleName();
        if (t instanceof ParameterizedType p) {
            return tipo(p.getRawType()) + "<" + Arrays.stream(p.getActualTypeArguments()).map(GeradorUml::tipo).collect(Collectors.joining(", ")) + ">";
        }
        if (t instanceof WildcardType w) return "?";
        return t.getTypeName();
    }

    static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
