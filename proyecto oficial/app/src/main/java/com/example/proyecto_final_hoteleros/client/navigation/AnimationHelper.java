// AnimationHelper.java
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
                return new AnimationSet(
                        R.anim.slide_in_bottom,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_bottom
                );

            case TOP_TO_BOTTOM:
                return new AnimationSet(
                        R.anim.slide_in_top,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_top
                );

            case FADE:
                return new AnimationSet(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                );

            case SCALE_UP:
                return new AnimationSet(
                        R.anim.scale_up_fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.scale_down_fade_out
                );

            case SLIDE_UP:
                return new AnimationSet(
                        R.anim.slide_in_bottom,
                        R.anim.stay,
                        R.anim.stay,
                        R.anim.slide_out_bottom
                );

            case NONE:
            default:
                return new AnimationSet(0, 0, 0, 0);
        }
    }
}