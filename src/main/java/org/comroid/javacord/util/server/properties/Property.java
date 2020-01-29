package org.comroid.javacord.util.server.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.comroid.javacord.util.model.container.ContainerAccessor;
import org.comroid.javacord.util.model.container.ValueContainer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.util.RawValue;
import org.intellij.lang.annotations.Language;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.server.Server;

public final class Property implements Nameable {
    public static final @Language("RegExp") String ANY_STRING = "(?s).*";
    public static final Map<Class<?>, String> DEFAULT_PATTERNS = new ConcurrentHashMap<Class<?>, String>() {{
        put(String.class, ANY_STRING);
        put(Integer.class, "\\d+");
        put(Long.class, "\\d+");
        put(Boolean.class, "(true)|(false)");
        put(Float.class, "\\d+(\\.\\d+)?");
        put(Double.class, "\\d+(\\.\\d+)?");
    }};

    private final Map<Long, ValueContainer> values = new ConcurrentHashMap<>(0);
    private final GuildSettings parent;
    private final String name;
    private final Class<?> type;
    private final Pattern pattern;
    private final ValueContainer defaultValue;
    private final PropertySerializer propertySerializer;

    public <T> Function<Long, T> function(Class<T> targetType) {
        return serverId -> targetType.cast(getValue(serverId).getRaw());
    }

    Property rebuild(Builder builder) {
        return new Property(
                parent,
                !builder.name.equals(name) ? builder.name : name,
                !builder.type.equals(type) ? builder.type : type,
                !builder.pattern.equals(pattern.pattern()) ? Pattern.compile(builder.pattern) : pattern,
                !builder.defaultValue.equals(defaultValue) ? builder.defaultValue : defaultValue,
                propertySerializer
        );
    }

    Property(GuildSettings parent, String name, Class<?> type, Pattern pattern, ValueContainer defaultValue, PropertySerializer propertySerializer) {
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.pattern = pattern;
        this.defaultValue = defaultValue;
        this.propertySerializer = propertySerializer;
    }

    @Override
    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public ContainerAccessor getDefaultValue() {
        return defaultValue;
    }

    public ContainerAccessor getValue(Server server) {
        return getValue(server.getId());
    }

    public ContainerAccessor getValue(long serverId) {
        return values.computeIfAbsent(serverId, key -> new ValueContainer(defaultValue.fallbackString()));
    }

    public boolean setRawValue(long serverId, Object raw) {
        if (pattern.matcher(String.valueOf(raw)).matches()) {
            ((ValueContainer) getValue(serverId)).setRaw(raw);

            return true;
        }

        return false;
    }

    public final JsonNode serialize() {
        return propertySerializer.serialize();
    }

    static Property from(GuildSettings parent, JsonNode data) throws ClassNotFoundException, NoSuchMethodException {
        // TODO: 28.01.2020 deserializer method
        final String name = data.get("name").asText();
        final Class<?> type = Class.forName(data.get("type").asText());
        final Pattern pattern = Pattern.compile(data.get("pattern").asText());
        final PropertySerializer propertySerializer = new PropertySerializer(data.get("serialization"));
        final ValueContainer defaultValue = propertySerializer.containerDeserializer.apply(data.get("defaultValue").asText());

        return new Property(parent, name, type, pattern, defaultValue, propertySerializer);
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME) @interface Serializer {

    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME) @interface Deserializer {
    }

    private static class PropertySerializer {
        private final Function<ValueContainer, String> containerSerializer;
        private final Function<String, ValueContainer> containerDeserializer;
        Property parent;

        PropertySerializer(Function<ValueContainer, String> containerSerializer, Function<String, ValueContainer> containerDeserializer) {
            this.containerSerializer = containerSerializer;
            this.containerDeserializer = containerDeserializer;
        }

        PropertySerializer(JsonNode data) throws ClassNotFoundException, NoSuchMethodException {
            // init serializer side
            if (data.has("serializer")) {
                final JsonNode serializerData = data.get("serializer");
                this.containerSerializer = new Function<ValueContainer, String>() {
                    private final Class<?> klass = Class.forName(serializerData.get("class").asText());
                    private final Method method = klass.getMethod(serializerData.get("method").asText(), ValueContainer.class);

                    @Override
                    public String apply(ValueContainer container) {
                        try {
                            return (String) method.invoke(null, container);
                        } catch (IllegalAccessException e) {
                            throw new AssertionError("Could not access serializer method: " + method.toGenericString(), e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException("Serializer threw an exception", e);
                        } catch (ClassCastException e) {
                            throw new AssertionError("Serializer returned an illegal object; must return " + String.class.getName(), e);
                        }
                    }
                };

                // init deserializer side
                final JsonNode deserializerData = data.get("deserializer");
                this.containerDeserializer = new Function<String, ValueContainer>() {
                    private final Class<?> klass = Class.forName(deserializerData.get("class").asText());
                    private final Method method = klass.getMethod(deserializerData.get("method").asText(), String.class);

                    @Override
                    public ValueContainer apply(String jsonNodes) {
                        try {
                            return (ValueContainer) method.invoke(null, jsonNodes);
                        } catch (IllegalAccessException e) {
                            throw new AssertionError("Could not access deserializer method: " + method.toGenericString(), e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException("Deserializer threw an exception", e);
                        } catch (ClassCastException e) {
                            throw new AssertionError("Deserializer returned an illegal object; must return " + ValueContainer.class.getName(), e);
                        }
                    }
                };
            } else {
                this.containerSerializer = ValueContainer::stringValue;
                this.containerDeserializer = ValueContainer::new;
            }
        }

        @SuppressWarnings("deprecation")
        public final JsonNode serialize() {
            final ObjectNode data = JsonNodeFactory.instance.objectNode();

            data.put("name", parent.name);
            data.put("type", parent.type.getName());
            data.put("pattern", parent.pattern.pattern());

            data.put("serialization", serializerData(parent.type));
            data.put("defaultValue", containerSerializer.apply(parent.defaultValue));

            final ObjectNode values = data.putObject("values");
            parent.values.forEach((serverId, container) -> values.put(String.valueOf(serverId), containerSerializer.apply(container)));

            return data;
        }

        public static PropertySerializer ofNative(Class<?> type) {
            class Local {
                private final Class<?> klass = type;
                private final Function<ValueContainer, String> serializer = ValueContainer::stringValue;
                private final Function<String, ValueContainer> deserializer = ValueContainer::new;
            }

            final Local local = new Local();
            return new PropertySerializer(local.serializer, local.deserializer);
        }

        private static JsonNode serializerData(Class<?> type) {
            final ObjectNode data = JsonNodeFactory.instance.objectNode();

            for (Method method : type.getMethods()) {
                if (method.isAnnotationPresent(Serializer.class) && validateSerializer(method)) {
                    // use as serializer
                    final ObjectNode serializerData = data.putObject("serializer");

                    serializerData.put("class", method.getDeclaringClass().getName());
                    serializerData.put("method", method.getName());
                } else if (method.isAnnotationPresent(Deserializer.class) && validateDeserializer(method)) {
                    // use as deserializer
                    final ObjectNode deserializerData = data.putObject("deserializer");

                    deserializerData.put("class", method.getDeclaringClass().getName());
                    deserializerData.put("method", method.getName());
                }
            }

            return data;
        }

        private static boolean validateSerializer(Method method) {
            final Class<?>[] parameterTypes = method.getParameterTypes();

            return ValueNode.class.isAssignableFrom(method.getReturnType())
                    && parameterTypes.length == 1
                    && ValueContainer.class.isAssignableFrom(parameterTypes[0]);
        }

        private static boolean validateDeserializer(Method method) {
            final Class<?>[] parameterTypes = method.getParameterTypes();

            return ValueContainer.class.isAssignableFrom(method.getReturnType())
                    && parameterTypes.length == 1
                    && ValueNode.class.isAssignableFrom(parameterTypes[0]);
        }
    }

    public static class Builder {
        private final GuildSettings parent;
        private String name;
        private Class<?> type;
        private String pattern;
        private ValueContainer defaultValue;

        Builder(GuildSettings parent) {
            this.parent = parent;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;

            return this;
        }

        public Class<?> getType() {
            return type;
        }

        public Builder setType(Class<?> type) {
            this.type = type;

            return this;
        }

        public String getPattern() {
            return pattern;
        }

        public Builder setPattern(@Language("RegExp") String pattern) {
            this.pattern = pattern;

            return this;
        }

        public ValueContainer getDefaultValue() {
            return defaultValue;
        }

        public Builder setDefaultValue(String defaultValue) {
            this.defaultValue = new ValueContainer(defaultValue);

            return this;
        }

        Property build() throws NoSuchMethodException, ClassNotFoundException {
            PropertySerializer propertySerializer;
            if (type.getPackage().getName().startsWith("java"))
                propertySerializer = PropertySerializer.ofNative(type);
            else propertySerializer = new PropertySerializer(PropertySerializer.serializerData(type));

            final Property property = new Property(parent, name, type, Pattern.compile(pattern), defaultValue, propertySerializer);
            propertySerializer.parent = property;

            return property;
        }
    }
}
