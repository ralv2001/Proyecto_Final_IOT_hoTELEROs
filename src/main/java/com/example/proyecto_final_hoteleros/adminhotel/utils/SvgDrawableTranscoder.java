package com.example.proyecto_final_hoteleros.adminhotel.utils;

import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.caverock.androidsvg.SVG;

public class SvgDrawableTranscoder implements ResourceTranscoder<SVG, PictureDrawable> {

    @Nullable
    @Override
    public Resource<PictureDrawable> transcode(@NonNull Resource<SVG> toTranscode,
                                               @NonNull Options options) {
        SVG svg = toTranscode.get();

        float width = svg.getDocumentWidth();
        float height = svg.getDocumentHeight();

        if (width <= 0) width = 512;
        if (height <= 0) height = 512;

        Picture picture = svg.renderToPicture((int) Math.ceil(width), (int) Math.ceil(height));
        PictureDrawable drawable = new PictureDrawable(picture);

        return new SimpleResource<>(drawable);
    }
}