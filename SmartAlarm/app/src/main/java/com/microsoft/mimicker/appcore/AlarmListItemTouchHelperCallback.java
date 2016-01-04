package com.microsoft.mimicker.appcore;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.microsoft.mimicker.R;

public class AlarmListItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;
    private boolean mCanDismiss;
    public AlarmListItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (mCanDismiss) {
            // Remove the item from the view
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        } else {
            // Reset the view back to its default visual state
            mAdapter.onItemDismissCancel(viewHolder.getAdapterPosition());
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(Canvas canvas, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            View itemView = viewHolder.itemView;
            Resources resources = AlarmApplication.getAppContext().getResources();
            Bitmap icon = BitmapFactory.decodeResource(resources, R.drawable.ic_delete_white_36dp);
            int iconPadding = resources.getDimensionPixelOffset(R.dimen.alarm_list_delete_icon_padding);
            int maxDrawWidth = (iconPadding * 2) + icon.getWidth();

            Paint paint = new Paint();
            paint.setColor(resources.getColor(R.color.red));

            int x = Math.round(Math.abs(dX));

            // Reset the dismiss flag if the view resets to its default position
            if (x == 0) {
                mCanDismiss = false;
            }

            // If we have travelled beyond twice the icon area via direct user interaction
            // we will dismiss when we get a swipe callback.  We do this to try to avoid
            // unwanted swipe dismissal
            if (x > (maxDrawWidth * 2) && isCurrentlyActive) {
                mCanDismiss = true;
            }

            int drawWidth  = Math.min(x, maxDrawWidth);

            if (dX > 0) {
                // Handle swiping to the right
                // Draw red background in area that we vacate up to maxDrawWidth
                canvas.drawRect((float) itemView.getLeft(),
                        (float) itemView.getTop(),
                        drawWidth,
                        (float) itemView.getBottom(),
                        paint);

                // Only draw icon when we've past the padding threshold
                if (x > iconPadding) {

                    Rect destRect = new Rect();
                    destRect.left = itemView.getLeft() + iconPadding;
                    destRect.top = itemView.getTop() + (itemView.getBottom() - itemView.getTop() - icon.getHeight()) / 2;
                    int maxRight = destRect.left + icon.getWidth();
                    destRect.right = Math.min(x, maxRight);
                    destRect.bottom = destRect.top + icon.getHeight();

                    // Only draw the appropriate parts of the bitmap as it is revealed
                    Rect srcRect = null;
                    if (x < maxRight) {
                        srcRect = new Rect();
                        srcRect.top = 0;
                        srcRect.left = 0;
                        srcRect.bottom = icon.getHeight();
                        srcRect.right = x - iconPadding;
                    }

                    canvas.drawBitmap(icon,
                            srcRect,
                            destRect,
                            paint);
                }

            } else {
                // Handle swiping to the left
                // Draw red background in area that we vacate  up to maxDrawWidth
                canvas.drawRect((float) itemView.getRight() - drawWidth,
                        (float) itemView.getTop(),
                        (float) itemView.getRight(),
                        (float) itemView.getBottom(), paint);

                // Only draw icon when we've past the padding threshold
                if (x > iconPadding) {
                    int fromLeftX = itemView.getRight() - x;
                    Rect destRect = new Rect();
                    destRect.right = itemView.getRight() - iconPadding;
                    destRect.top = itemView.getTop() + (itemView.getBottom() - itemView.getTop() - icon.getHeight()) / 2;
                    int maxFromLeft = destRect.right - icon.getWidth();
                    destRect.left = Math.max(fromLeftX, maxFromLeft);
                    destRect.bottom = destRect.top + icon.getHeight();

                    // Only draw the appropriate parts of the bitmap as it is revealed
                    Rect srcRect = null;
                    if (fromLeftX > maxFromLeft) {
                        srcRect = new Rect();
                        srcRect.top = 0;
                        srcRect.right = icon.getWidth();
                        srcRect.bottom = icon.getHeight();
                        srcRect.left = srcRect.right - (x - iconPadding);
                    }

                    canvas.drawBitmap(icon,
                            srcRect,
                            destRect,
                            paint);
                }
            }

            // Fade out the item as we swipe it
            float alpha = 1.0f - Math.abs(dX) / (float) itemView.getWidth();
            itemView.setAlpha(alpha);
            itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    public interface ItemTouchHelperAdapter {
        void onItemDismiss(int position);

        void onItemDismissCancel(int position);
    }
}