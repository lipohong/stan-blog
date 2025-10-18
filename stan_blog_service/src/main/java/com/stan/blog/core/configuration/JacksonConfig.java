package com.stan.blog.core.configuration;

import java.io.IOException;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

    public static class PageJsonSerializer extends JsonSerializer<Page> {
        @Override
        public void serialize(Page value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("current", value.getNumber() + 1);
            gen.writeNumberField("size", value.getSize());
            gen.writeNumberField("total", value.getTotalElements());
            gen.writeNumberField("pages", value.getTotalPages());
            gen.writeFieldName("records");
            gen.writeObject(value.getContent());
            gen.writeEndObject();
        }
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer pageJacksonCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            PageJsonSerializer serializer = new PageJsonSerializer();
            module.addSerializer(Page.class, serializer);
            module.addSerializer(PageImpl.class, serializer);
            builder.modules(module);
            builder.serializerByType(Page.class, serializer);
            builder.serializerByType(PageImpl.class, serializer);
        };
    }
}