package com.personal.marketnote.user.adapter.out.hibernate;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import lombok.NoArgsConstructor;
import org.hibernate.collection.spi.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

@NoArgsConstructor
public class HibernateCollectionIdResolver extends TypeIdResolverBase {

    @Override
    public String idFromValue(Object value) {
        if (value instanceof PersistentArrayHolder) {
            return Array.class.getName();
        }

        if (value instanceof PersistentBag || value instanceof PersistentIdentifierBag
                || value instanceof PersistentList) {
            return List.class.getName();
        }

        if (value instanceof PersistentSortedMap) {
            return TreeMap.class.getName();
        }

        if (value instanceof PersistentSortedSet) {
            return TreeSet.class.getName();
        }

        if (value instanceof PersistentMap) {
            return HashMap.class.getName();
        }

        if (value instanceof PersistentSet) {
            return HashSet.class.getName();
        }

        return value.getClass().getName();
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }

    @Override
    public JavaType typeFromId(DatabindContext ctx, String id) throws IOException {
        try {
            return ctx.getConfig().constructType(Class.forName(id));
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CLASS;
    }
}
