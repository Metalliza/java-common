package common.basic.utils;

import java.util.ArrayList;
import java.util.List;

public class EnumUtil {
    public static <E extends Enum<E>> String getFullName(Enum<E> e) {
        if (e == null)
            return "";

        return e.getDeclaringClass().getCanonicalName() + "." + e.name();
    }

    public static <E extends Enum<E>> E parse(Class<E> clazz, String s) {
        if (StringUtil.isNullOrEmpty(s))
            return null;

        try {
            return Enum.valueOf(clazz, s);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static <E extends Enum<E>> List<E> parse(Class<E> clazz, List<String> listValue) {
        if (null == listValue || 0 == listValue.size())
            return new ArrayList<E>();

        ArrayList<E> list = new ArrayList<E>();
        for (String value : listValue) {
            E e = parse(clazz, value);
            if (null != e)
                list.add(e);
        }
        return list;
    }

    public static <E extends Enum<E>> boolean contains(List<E> list, E other) {
        for (E e : list) {
            if (e.equals(other))
                return true;
        }

        return false;
    }

    public static <E extends Enum<E>> String toString(E value) {

        if (null == value)
            return "";

        return value.toString();
    }

    public static <T extends Enum<T>, F extends Enum<F>> F convertTF(Class<F> clazz, T t) {
        if (t == null)
            return null;

        return parse(clazz, t.toString());
    }
}
