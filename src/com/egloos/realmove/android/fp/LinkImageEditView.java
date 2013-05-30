
package com.egloos.realmove.android.fp;

import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.model.Link.SelectArea;
import com.egloos.realmove.android.fp.model.RectPosition;
import com.egloos.realmove.android.fp.util.Util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;

public class LinkImageEditView extends LinkImageView implements OnMenuItemClickListener {

    private static final int DRAG_THRESHOLD_X = 15;
    private static final int DRAG_THRESHOLD_Y = 15;

    private static final String TAG = LinkImageEditView.class.getSimpleName();

    private static final int NEW_LINK_WIDTH_DP = 80;
    private static final int NEW_LINK_HEIGHT_DP = 80;

    private OnLinkChangeListener mListener;

    private final Paint paint2;
    private final float circleRadius; // 코너 그릴때의 반지름
    private final float circleRadiusSelected; // 코너 그릴때의 반지름

    private float rx = -1F;
    private float ry = -1F;

    public LinkImageEditView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint2 = new Paint();
        paint2.setStrokeWidth(Util.dpToPx(context, 1.5F));

        circleRadius = Util.dpToPx(context, 3);
        circleRadiusSelected = Util.dpToPx(context, 6);
    }

    private Link selected;
    private float prevX, prevY;

    private void selectLink(Link link) {
        selected = link;
        invalidate();
    }

    private Link unselectLink() {
        Link link = selected;
        selected = null;
        invalidate();
        return link;
    }

    enum State {
        READY // UP이벤트: 터치되지 않은 상태

        , SELECTING_LINK // DOWN이벤트: 터치된 상태
        , SELECTING_CORNER // DOWN이벤트: 모서리가 터치된 상태
        , SELECTING_TWICE // DOWN이벤트: 두번째 터치된 상태 - UP되면 unselect
        , SELECTING_EMPTY // DOWN이벤트: 빈 곳을 터치한 상태

        , SHOWING_CONTEXT_MENU // ContextMenu 가 표시된 상태

        , MOVING // MOVE이벤트: 드래그해서 움직이고 있는 상태
        , RESIZING // MOVE이벤트: 모서리 선택해서 리사이즈 되는 상태
    }

    private State state = State.READY;

    private void setState(State state) {
        this.state = state;
        FpLog.d(TAG, "setState()", state);
    }

    private State getState() {
        return this.state;
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        // FpLog.d( TAG, "onCreatedContextMenu()" );
        MenuInflater inflater = new MenuInflater(this.getContext());
        if (getState() == State.SELECTING_EMPTY) {
            inflater.inflate(R.menu.menu_link_edit_context_empty, menu);
        } else {
            inflater.inflate(R.menu.menu_link_edit_context, menu);
        }

        int size = menu.size();
        for (int i = 0; i < size; i++) {
            menu.getItem(i).setOnMenuItemClickListener(this);
        }

        setState(State.SHOWING_CONTEXT_MENU);
        super.onCreateContextMenu(menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.link_to:
                LinkSelectDialog.show(getContext(), selected);
                break;
            case R.id.remove:
                if (selected != null) {
                    removeSelectedLink();
                }
                break;
            case R.id.create:
                createNewLink(prevX, prevY);
                break;
        }
        return false;
    }

    private void removeSelectedLink() {
        Link link = selected;
        mLinks.remove(selected);
        selected = null;
        setState(State.READY);
        invalidate();

        if (mListener != null) {
            mListener.linkRemoved(link);
        }
    }

    final Handler handler = new Handler();
    Runnable mLongTap = new Runnable() {
        public void run() {
            showContextMenu();
        }
    };

    private void startLongtapTimer() {
        handler.postDelayed(mLongTap, 300);
    }

    private void stopLongtapTimer() {
        handler.removeCallbacks(mLongTap);
    }

    /** ( vertical, horizontal ) */
    static final SelectArea[][] AREA_METRIX = {
            {
                    SelectArea.CORNER_LT, SelectArea.LINE_TOP, SelectArea.CORNER_RT
            },
            {
                    SelectArea.LINE_LEFT, SelectArea.CENTER, SelectArea.LINE_RIGHT
            },
            {
                    SelectArea.CORNER_LB, SelectArea.LINE_BOTTOM, SelectArea.CORNER_RB
            }
    };

    SelectArea selectedArea = SelectArea.CENTER;

    /** dstRect의 수평방향 중간점 */
    private int hHalf;

    /** dstRect의 수직방향 중간점 */
    private int vHalf;

    protected SelectArea checkCorner(Link link, float x, float y) {
        // dstRect 는 이미 getSelectedLink() 에서 구해져있어야 한다.

        int hType = 0; // 0:왼쪽, 1:중간, 2:오른쪽
        int vType = 0; // 0:위, 1:중간, 2:아래

        if (x <= dstRect.getLeft() + CORNER_WIDTH)
            hType = 0;
        else if (x >= dstRect.getRight() - CORNER_WIDTH)
            hType = 2;
        else
            hType = 1;

        if (y <= dstRect.getTop() + CORNER_WIDTH)
            vType = 0;
        else if (y >= dstRect.getBottom() - CORNER_WIDTH)
            vType = 2;
        else
            vType = 1;

        return AREA_METRIX[vType][hType];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                startLongtapTimer();
                Link link = getTouchedLink(event.getX(), event.getY());
                prevX = toLogicalPosX(event.getX());
                prevY = toLogicalPosY(event.getY());
                if (link != null) {
                    if (selected == link) {
                        selectedArea = checkCorner(link, event.getX(), event.getY());
                        if (selectedArea == null || selectedArea == SelectArea.CENTER) {
                            setState(State.SELECTING_TWICE);
                        } else {
                            setState(State.SELECTING_CORNER);
                        }
                    } else {
                        selectLink(link);
                        setState(State.SELECTING_LINK);
                    }
                } else {
                    setState(State.SELECTING_EMPTY);
                    unselectLink();
                }
                invalidate();
                break;
            }

            case MotionEvent.ACTION_UP: {
                stopLongtapTimer();
                if (getState() == State.SELECTING_TWICE) {
                    unselectLink();
                } else if (getState() == State.SELECTING_EMPTY) {
                    createNewLink(event.getX(), event.getY());
                } else if (getState() == State.MOVING || getState() == State.RESIZING) {
                    if (mListener != null) {
                        mListener.linkModified(selected);
                    }
                }
                setState(State.READY);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float x = toLogicalPosX(event.getX());
                float y = toLogicalPosY(event.getY());

                float dx = x - prevX;
                float dy = y - prevY;

                if (selected != null) {
                    switch (getState()) {
                        case SELECTING_LINK:
                            if ((dx > -DRAG_THRESHOLD_X && dx < DRAG_THRESHOLD_X)
                                    || (dy > -DRAG_THRESHOLD_Y && dy < DRAG_THRESHOLD_Y)) {
                                // ignore this
                                break;
                            }
                            stopLongtapTimer();
                            setState(State.MOVING);
                            break;

                        case SELECTING_TWICE: {
                            stopLongtapTimer();
                            setState(State.MOVING);
                            break;
                        }
                        case SELECTING_CORNER: {
                            stopLongtapTimer();
                            setState(State.RESIZING);
                            break;
                        }
                        case MOVING: {
                            if (x > 0 && y > 0 && x < Link.MAX_WIDTH && y < Link.MAX_HEIGHT) {
                                selected.move(dx, dy);
                                invalidate();
                                prevX = x;
                                prevY = y;
                            }
                            break;
                        }
                        case RESIZING: {
                            if (x > 0 && y > 0 && x < Link.MAX_WIDTH && y < Link.MAX_HEIGHT) {
                                selected.resize(selectedArea, dx, dy);
                                invalidate();
                                prevX = x;
                                prevY = y;
                            }
                            break;
                        }
                        default: {
                            setState(State.READY);
                            break;
                        }
                    }
                } else {
                    if ((dx > -DRAG_THRESHOLD_X && dx < DRAG_THRESHOLD_X)
                            || (dy > -DRAG_THRESHOLD_Y && dy < DRAG_THRESHOLD_Y)) {
                        // ignore this
                    } else {
                        setState(State.READY);
                    }
                }
                break;
            }
        }
        return true;
    }

    /**
     * 실제 화면 좌표를, Link 의 논리좌표로 변환한다.
     * 
     * @param x
     * @return
     */
    private float toLogicalPosX(float x) {
        if (rx == -1F) {
            rx = (float) Link.MAX_WIDTH / getWidth();
        }
        return x * rx;

    }

    /**
     * 실제 화면 좌표를, Link 의 논리좌표로 변환한다.
     * 
     * @param y
     * @return
     */
    private float toLogicalPosY(float y) {
        if (ry == -1F) {
            ry = (float) Link.MAX_HEIGHT / getHeight();
        }
        return y * ry;

    }

    private Link createNewLink(float x, float y) {
        Link link = new Link();
        RectPosition rect = new RectPosition();

        float linkWidthHalf = Util.dpToPx(getContext(), NEW_LINK_WIDTH_DP) / 2;
        float linkHeightHalf = Util.dpToPx(getContext(), NEW_LINK_HEIGHT_DP) / 2;

        float left = Math.max(0, toLogicalPosX(x - linkWidthHalf));
        float right = Math.min(Link.MAX_WIDTH - 1, toLogicalPosX(x + linkWidthHalf));
        float top = Math.max(0, toLogicalPosY(y - linkHeightHalf));
        float bottom = Math.min(Link.MAX_HEIGHT - 1, toLogicalPosY(y + linkHeightHalf));

        rect.setLeft((int) left);
        rect.setRight((int) right);
        rect.setTop((int) top);
        rect.setBottom((int) bottom);

        link.setPosition(rect);

        mLinks.add(link);

        invalidate();

        if (mListener != null) {
            mListener.linkAdded(link);
        }

        return link;
    }

    @Override
    protected void drawLink(Canvas canvas, Paint paint, Link link) {
        super.drawLink(canvas, paint, link);
        if (selected != null && selected == link) {
            // dstRect 는 super의 drawLink 에서 구했음.
            paint2.setColor(0xff33b5e5);

            float[] pts = new float[] {
                    dstRect.getLeft(), dstRect.getTop(),
                    dstRect.getRight(), dstRect.getTop(),
                    dstRect.getRight(), dstRect.getTop(),
                    dstRect.getRight(), dstRect.getBottom(),
                    dstRect.getRight(), dstRect.getBottom(),
                    dstRect.getLeft(), dstRect.getBottom(),
                    dstRect.getLeft(), dstRect.getBottom(),
                    dstRect.getLeft(), dstRect.getTop()
            };

            canvas.drawLines(pts, paint2);

            hHalf = dstRect.getLeft() + (dstRect.getRight() - dstRect.getLeft()) / 2;
            vHalf = dstRect.getTop() + (dstRect.getBottom() - dstRect.getTop()) / 2;

            if (getState() == State.RESIZING || getState() == State.SELECTING_CORNER) {
                paint2.setColor(0xff33b5e5);
                drawCircle(canvas, circleRadiusSelected, paint2, selectedArea);

                paint2.setColor(0xff0099cc);
                drawCircles(canvas, circleRadius, paint2);
            } else {
                drawCircles(canvas, circleRadius, paint2);
            }

        }
    }

    private void drawCircles(Canvas canvas, float radius, Paint paint) {
        SelectArea[] areas = SelectArea.values();
        for (SelectArea area : areas) {
            drawCircle(canvas, radius, paint, area);
        }
    }

    private void drawCircle(Canvas canvas, float radius, Paint paint, SelectArea area) {
        if (area != null) {
            switch (area) {
                case CORNER_LT:
                    canvas.drawCircle(dstRect.getLeft(), dstRect.getTop(), radius, paint2);
                    break;
                case LINE_TOP:
                    canvas.drawCircle(hHalf, dstRect.getTop(), radius, paint2);
                    break;
                case CORNER_RT:
                    canvas.drawCircle(dstRect.getRight(), dstRect.getTop(), radius, paint2);
                    break;
                case LINE_LEFT:
                    canvas.drawCircle(dstRect.getLeft(), vHalf, radius, paint2);
                    break;
                case LINE_RIGHT:
                    canvas.drawCircle(dstRect.getRight(), vHalf, radius, paint2);
                    break;
                case CORNER_LB:
                    canvas.drawCircle(dstRect.getLeft(), dstRect.getBottom(), radius, paint2);
                    break;
                case LINE_BOTTOM:
                    canvas.drawCircle(hHalf, dstRect.getBottom(), radius, paint2);
                    break;
                case CORNER_RB:
                    canvas.drawCircle(dstRect.getRight(), dstRect.getBottom(), radius, paint2);
                    break;
                case CENTER:
                default:
                    break;

            }
        }
    }

    public void setOnLinkChangeListener(OnLinkChangeListener listener) {
        mListener = listener;
    }

    public interface OnLinkChangeListener {
        public void linkAdded(Link link);

        public void linkRemoved(Link link);

        public void linkModified(Link link);
    }

}
