package br.com.fiap.bairroativo.service;

import br.com.fiap.bairroativo.exception.ValidacaoException;

import java.util.Locale;
import java.util.regex.Pattern;

/** Validações de entrada reaproveitadas pelos services. Todas lançam {@link ValidacaoException} com texto claro. */
public final class Validador {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern TELEFONE = Pattern.compile("^[0-9()+\\-\\s]{10,20}$");

    private Validador() {
    }

    /** Campo obrigatório: remove espaços das pontas e confere o tamanho. */
    public static String texto(String valor, String campo, int minimo, int maximo) {
        String limpo = valor == null ? "" : valor.trim();
        if (limpo.isEmpty()) {
            throw new ValidacaoException("Informe " + campo + ".");
        }
        if (limpo.length() < minimo) {
            throw new ValidacaoException(capitalizar(campo) + " deve ter pelo menos " + minimo + " caracteres.");
        }
        if (limpo.length() > maximo) {
            throw new ValidacaoException(capitalizar(campo) + " deve ter no máximo " + maximo + " caracteres.");
        }
        return limpo;
    }

    /** Campo opcional: vazio vira null. */
    public static String textoOpcional(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return texto(valor, campo, 1, maximo);
    }

    public static String email(String valor) {
        String email = texto(valor, "o e-mail", 6, 120).toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(email).matches()) {
            throw new ValidacaoException("Informe um e-mail válido, por exemplo nome@email.com.");
        }
        return email;
    }

    public static String telefoneOpcional(String valor) {
        String telefone = textoOpcional(valor, "o telefone", 20);
        if (telefone != null) {
            long digitos = telefone.chars().filter(Character::isDigit).count();
            if (!TELEFONE.matcher(telefone).matches() || digitos < 10 || digitos > 13) {
                throw new ValidacaoException("Telefone inválido. Use DDD + número, por exemplo (11) 91234-5678.");
            }
        }
        return telefone;
    }

    public static long id(Long valor, String campo) {
        if (valor == null || valor <= 0) {
            throw new ValidacaoException("Selecione " + campo + ".");
        }
        return valor;
    }

    public static <E extends Enum<E>> E opcao(String valor, Class<E> tipo, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("Selecione " + campo + ".");
        }
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("Opção inválida para " + campo + ": " + valor + ".");
        }
    }

    public static void corpo(Object corpo) {
        if (corpo == null) {
            throw new ValidacaoException("Envie os dados do formulário.");
        }
    }

    private static String capitalizar(String campo) {
        String semArtigo = campo.replaceFirst("^(o|a|os|as) ", "");
        return Character.toUpperCase(semArtigo.charAt(0)) + semArtigo.substring(1);
    }
}
