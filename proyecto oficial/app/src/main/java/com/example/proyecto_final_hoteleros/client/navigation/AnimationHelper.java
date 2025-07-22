// AnimationHelper.java - SEGURO: Solo usa animaciones que sabemos que existen
package com.example.proyecto_final_hoteleros.client.navigation;

import com.example.proyecto_final_hoteleros.R;

public class AnimationHelper {

    public static class AnimationSet {
        public final int enter;
        public final int exit;
        public final int popEnter;
        public final int popExit;

        public AnimationSet(int enter, int exit, int popEnter, int popExit) {
            this.enter = enter;
            this.exit = exit;
            this.popEnter = popEnter;
            this.popExit = popExit;
        }
    }

    public static AnimationSet getAnimationSet(AnimationDirection direction) {
        switch (direction) {
            case LEFT_TO_RIGHT:
                return new AnimationSet(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );

            case RIGHT_TO_LEFT:
                return new AnimationSet(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                );

            case BOTTOM_TO_TOP:
                // Usar animaciones existentes como fallback
                return new AnimationSet(
                        R.anim.slide_in_right, // Fallback
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_left  // Fallback
                );

            case TOP_TO_BOTTOM:
                return new AnimationSet(
                        R.anim.slide_in_left,  // Fallback
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_right // Fallback
                );

            case FADE:
                return new AnimationSet(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                );

            case SCALE_UP:
                // Usar fade como fallback si no existe scale
                return new AnimationSet(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                );

            case SLIDE_UP:
                return new AnimationSet(
                        R.anim.slide_in_right, // Fallback
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_left  // Fallback
                );

            case NONE:
            default:
                return new AnimationSet(0, 0, 0, 0);
        }
    }
}