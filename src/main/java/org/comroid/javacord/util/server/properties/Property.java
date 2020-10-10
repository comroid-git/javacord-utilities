package org.comroid.javacord.util.server.properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.comroid.javacord.util.model.container.ContainerAccessor;
import org.comroid.javacord.util.model.container.ValueContainer;
import org.intellij.lang.annotations.Language;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.server.Server;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class Property implements Nameable {
    public static final @Language("RegExp")
    String ANY_STRING = "(?s).*";
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
    private @Nullable String description;

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
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

    Property(GuildSettings parent, String name, Class<?> type, Pattern pattern, ValueContainer defaultValue, PropertySerializer propertySerializer) {
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.pattern = pattern;
        this.defaultValue = defaultValue;
        this.propertySerializer = propertySerializer;
    }

    static Property from(GuildSettings parent, JsonNode data) throws ClassNotFoundException, NoSuchMethodException {
        final String name = data.get("name").asText();
        final Class<?> type = Class.forName(data.get("type").asText());
        final Pattern pattern = Pattern.compile(data.get("pattern").asText());
        final PropertySerializer propertySerializer = new PropertySerializer(data.get("serialization"));
        final String defaultStringValue = data.get("defaultValue").asText();
        final ValueContainer defaultValue = propertySerializer.containerDeserializer.apply(defaultStringValue, defaultStringValue);

        final Property property = new Property(parent, name, type, pattern, defaultValue, propertySerializer);
        propertySerializer.parent = property;

        Optional.ofNullable(data.path("description").asText(null)).ifPresent(property::setDescription);

        if (data.has("values")) {
            final JsonNode values = data.get("values");
            final Iterator<String> serverIds = values.fieldNames();

            serverIds.forEachRemaining(id -> {
                final String stringValue = values.get(id).asText();
                final ValueContainer container = propertySerializer.containerDeserializer.apply(defaultStringValue, stringValue);

                property.values.put(Long.parseLong(id), container);
            });
        }

        return property;
    }

    public <T> Function<Long, T> function(Class<T> targetType) {
        return serverId -> targetType.cast(getValue(serverId).getRaw());
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

    Property rebuild(Builder builder) {
        final Property property = new Property(
                parent,
                !builder.name.equals(name) ? builder.name : name,
                !builder.type.equals(type) ? builder.type : type,
                !builder.pattern.equals(pattern.pattern()) ? Pattern.compile(builder.pattern) : pattern,
                !builder.defaultValue.equals(defaultValue) ? builder.defaultValue : defaultValue,
                propertySerializer
        );

        if (!builder.description.equals(description))
            property.setDescription(builder.description);

        return property;
    }

    public final JsonNode serialize() {
        return propertySerializer.serialize();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Serializer {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Deserializer {
    }

    private static class PropertySerializer {
        private final Function<ValueContainer, String> containerSerializer;
        private final BiFunction<String, Object, ValueContainer> containerDeserializer;
        Property parent;

        PropertySerializer(Function<ValueContainer, String> containerSerializer, BiFunction<String, Object, ValueContainer> containerDeserializer) {
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
                this.containerDeserializer = new BiFunction<String, Object, ValueContainer>() {
                    private final Class<?> klass = Class.forName(deserializerData.get("class").asText());
                    private final Method method = klass.getMethod(deserializerData.get("method").asText(), String.class, Object.class);

                    @Override
                    public ValueContainer apply(String defaultValue, Object value) {
                        try {
                            return (ValueContainer) method.invoke(null, defaultValue, value);
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

        public static PropertySerializer ofNative(Class<?> type) {
            class Local {
                private final Class<?> klass = type;
                private final Function<ValueContainer, String> serializer = ValueContainer::stringValue;
                private final BiFunction<String, Object, ValueContainer> deserializer = ValueContainer::new;
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

            return String.class.isAssignableFrom(method.getReturnType())
                    && parameterTypes.length == 1
                    && ValueContainer.class.isAssignableFrom(parameterTypes[0]);
        }

        private static boolean validateDeserializer(Method method) {
            final Class<?>[] parameterTypes = method.getParameterTypes();

            return ValueContainer.class.isAssignableFrom(method.getReturnType())
                    && parameterTypes.length == 2
                    && String.class.isAssignableFrom(parameterTypes[0])
                    && Object.class.isAssignableFrom(parameterTypes[1]);
        }

        @SuppressWarnings("deprecation")
        public final JsonNode serialize() {
            final ObjectNode data = JsonNodeFactory.instance.objectNode();

            data.put("name", parent.name);
            parent.getDescription().ifPresent(str -> data.put("description", str));
            data.put("type", parent.type.getName());
            data.put("pattern", parent.pattern.pattern());

            data.put("serialization", serializerData(parent.type));
            data.put("defaultValue", containerSerializer.apply(parent.defaultValue));

            final ObjectNode values = data.putObject("values");
            parent.values.forEach((serverId, container) -> values.put(String.valueOf(serverId), containerSerializer.apply(container)));

            return data;
        }
    }

    public static class Builder {
        private final GuildSettings parent;
        private String name;
        private String description;
        private Class<?> type;
        private String pattern;
        private ValueContainer defaultValue;

        public String getDescription() {
            return description;
        }

        public Builder setDescription(String description) {
            this.description = description;

            return this;
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
            this.defaultValue = new ValueContainer(defaultValue, defaultValue);

            return this;
        }

        Builder(GuildSettings parent) {
            this.parent = parent;
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
