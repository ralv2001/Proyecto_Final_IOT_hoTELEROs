package com.example.proyecto_final_hoteleros.adminhotel.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.io.InputStream;

public class SvgDecoder implements ResourceDecoder<InputStream, SVG> {

    @Override
    public boolean handles(@NonNull InputStream source, @NonNull Options options) {
        return true;
    }

    @Nullable
    @Override
    public Resource<SVG> decode(@NonNull InputStream source, int width, int height,
                                @NonNull Options options) throws IOException {
        try {
            SVG svg = SVG.getFromInputStream(source);

            // Configurar dimensiones si son especificadas
            if (width != Target.SIZE_ORIGINAL && width > 0) {
                svg.setDocumentWidth(width);
            }
            if (height != Target.SIZE_ORIGINAL && height > 0) {
                svg.setDocumentHeight(height);
            }

            // Establecer dimensiones predeterminadas si no están definidas
            if (svg.getDocumentWidth() <= 0) {
                svg.setDocumentWidth(512);
            }
            if (svg.getDocumentHeight() <= 0) {
                svg.setDocumentHeight(512);
            }

            return new SimpleResource<>(svg);
        } catch (SVGParseException ex) {
            throw new IOException("Cannot load SVG from stream", ex);
        } finally {
            try {
                source.close();
            } catch (IOException ignored) {
                // Ignorar errores al cerrar
            }
        }
    }
}