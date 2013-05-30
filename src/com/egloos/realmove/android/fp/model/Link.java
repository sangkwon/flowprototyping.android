
package com.egloos.realmove.android.fp.model;

import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.model.Link.SelectArea;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

public class Link implements Serializable {

    private static final long serialVersionUID = 5122289147217498540L;
    private static final String TAG = Link.class.getSimpleName();

    public enum Event {
        TOUCH, LONG_TOUCH, SWIPE_LEFT, SWIPE_RIGHT
    };

    public enum Anim {
        NONE, FADE_IN, FADE_OUT, SLIDE_UP, SLIDE_DOWN, SLIDE_LEFT, SLIDE_RIGHT, ZOOM_IN, ZOOM_OUT
    };

    public enum SelectArea {
        LINE_LEFT, LINE_RIGHT, LINE_TOP, LINE_BOTTOM,
        CORNER_LT, CORNER_RT, CORNER_LB, CORNER_RB, CENTER
    }

    private int pageId;
    private int id;
    private Event event = Event.TOUCH;
    private Anim anim = Anim.NONE;
    private int targetPageId = -1;
    private RectPosition position;

    public static final int MAX_WIDTH = 720;
    public static final int MAX_HEIGHT = 1280;

    /**
     * MAX_WIDTH x MAX_HEIGHT 로 반환된 좌표 반환
     * 
     * @param width
     * @param height
     * @return
     */
    public void getPosition(RectPosition dstRect, int width, int height) {
        Link.convPosition(this.getPosition(), MAX_WIDTH, MAX_HEIGHT, dstRect, width, height);
    }

    /**
     * 폭과 높이가 다른 좌표계를 서로 상호변환
     * 
     * @param srcRect
     * @param srcWidth
     * @param srcHeight
     * @param dstRect
     * @param dstWidth
     * @param dstHeight
     */
    public static void convPosition(RectPosition srcRect, int srcWidth, int srcHeight,
            RectPosition dstRect, int dstWidth, int dstHeight) {
        float pWidth = (float) dstWidth / srcWidth;
        float pHeight = (float) dstHeight / srcHeight;

        dstRect.setLeft(Math.round(srcRect.getLeft() * pWidth));
        dstRect.setRight(Math.round(srcRect.getRight() * pWidth));
        dstRect.setTop(Math.round(srcRect.getTop() * pHeight));
        dstRect.setBottom(Math.round(srcRect.getBottom() * pHeight));
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * 720 x 1280 화면으로 환산된 좌표를 반환
     * 
     * @return
     */
    public RectPosition getPosition() {
        return position;
    }

    public void setPosition(RectPosition position) {
        this.position = position;
    }

    public int getTargetPageId() {
        return targetPageId;
    }

    public void setTargetPageId(int target) {
        this.targetPageId = target;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Anim getAnim() {
        return anim;
    }

    public void setAnim(Anim anim) {
        this.anim = anim;
    }

    public JSONObject toJSONObject() {
        try {
            JSONObject jsonObj = new JSONObject();

            jsonObj.putOpt("pageId", this.getPageId());
            jsonObj.putOpt("id", this.getId());

            if (this.getEvent() != null)
                jsonObj.putOpt("event", this.getEvent().name());

            if (this.getAnim() != null)
                jsonObj.putOpt("anim", this.getAnim().name());

            jsonObj.putOpt("target", this.getTargetPageId());
            if (this.getPosition() != null)
                jsonObj.putOpt("position", this.getPosition().toJSONObject());

            return jsonObj;
        } catch (Exception ex) {
            FpLog.e(TAG, ex);
            return null;
        }
    }

    public static Link fromJSONObject(JSONObject jsonObj) {
        try {
            Link link = new Link();

            link.setPageId(jsonObj.optInt("pageId"));
            link.setId(jsonObj.getInt("id"));
            link.setEvent(Event.valueOf(jsonObj.optString("event")));
            link.setAnim(Anim.valueOf(jsonObj.optString("anim")));
            link.setTargetPageId(jsonObj.optInt("target", Integer.MIN_VALUE));
            link.setPosition(RectPosition.fromJSONObject(jsonObj.optJSONObject("position")));
            return link;
        } catch (Exception ex) {
            FpLog.e(TAG, ex);
        }
        return null;
    }

    public boolean checkPos(int left, int right, int top, int bottom) {
        FpLog.d(TAG, "checkPos()", right - left, bottom - top);
        if (left >= 0 && right < MAX_WIDTH && top >= 0 && bottom < MAX_HEIGHT && right - left > 96
                && bottom - top > 96) {
            position.setLeft(left);
            position.setRight(right);
            position.setTop(top);
            position.setBottom(bottom);
            return true;
        }

        return false;
    }

    /**
     * @param dx
     * @param dy
     * @return 만일 테두리에 걸렸다면 false
     */
    public boolean move(float dx, float dy) {
        int left = position.getLeft() + (int) dx;
        int right = position.getRight() + (int) dx;
        int top = position.getTop() + (int) dy;
        int bottom = position.getBottom() + (int) dy;

        return checkPos(left, right, top, bottom);
    }

    public boolean resize(SelectArea area, float dx, float dy) {
        int left = position.getLeft();
        int right = position.getRight();
        int top = position.getTop();
        int bottom = position.getBottom();

        switch (area) {
            case CORNER_LB:
                left += (int) dx;
                bottom += (int) dy;
                break;
            case CORNER_LT:
                left += (int) dx;
                top += (int) dy;
                break;
            case CORNER_RB:
                right += (int) dx;
                bottom += (int) dy;
                break;
            case CORNER_RT:
                right += (int) dx;
                top += (int) dy;
                break;
            case LINE_BOTTOM:
                bottom += (int) dy;
                break;
            case LINE_LEFT:
                left += (int) dx;
                break;
            case LINE_RIGHT:
                right += (int) dx;
                break;
            case LINE_TOP:
                top += (int) dy;
                break;
            case CENTER:
            default:
                // do nothing
                break;
        }

        return checkPos(left, right, top, bottom);
    }

}
