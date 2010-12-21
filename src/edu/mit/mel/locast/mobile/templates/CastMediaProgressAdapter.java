package edu.mit.mel.locast.mobile.templates;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import edu.mit.mel.locast.mobile.data.CastMedia;
import edu.mit.mel.locast.mobile.data.ShotList;
import edu.mit.mobile.android.widget.RelativeSizeListAdapter;

public class CastMediaProgressAdapter extends CursorAdapter implements RelativeSizeListAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = CastMediaProgressAdapter.class.getSimpleName();
	private int[] progress;
	private int[] shotLength;


	private final int durationCol, videoCol;
	private final int shotListDurationCol;
	private final Cursor shotListCursor;

	public CastMediaProgressAdapter(Context context, Cursor castMedia, Cursor shotList) {
		super(context, castMedia);

		durationCol = castMedia.getColumnIndex(CastMedia._DURATION);
		videoCol = castMedia.getColumnIndex(CastMedia._LOCAL_URI);

		this.shotListCursor = shotList;
		shotListDurationCol = shotList.getColumnIndex(ShotList._DURATION);

		loadProgress();
	}

	private void loadProgress(){
		final Cursor c = getCursor();
		if (c.isClosed() || shotListCursor.isClosed()){
			return;
		}
		progress = new int[getCount()];
		shotLength = new int[getCount()];

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			final int position = c.getPosition();
			progress[position] = !c.isNull(videoCol) ? c.getInt(durationCol) : 0;

			int shotLen = c.getInt(durationCol);
			if (shotLen == 0){
				shotListCursor.moveToPosition(position);
				shotLen = shotListCursor.getInt(shotListDurationCol) * 1000;
			}
			shotLength[position] = shotLen;
		}
	}

	@Override
	protected void onContentChanged() {
		super.onContentChanged();

		loadProgress();
	}

	/**
	 * @param position the number of the cast media
	 * @param progress progress in milliseconds.
	 */
	public void updateProgress(int position, int progress){
		this.progress[position] = progress;
		notifyDataSetChanged();
	}

	@Override
	public float getRelativeSize(int position) {
		float len = shotLength[position];
		if (len == 0){
			len = 3000; // XXX this should really be in the presentation layer.
		}
		return len;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ProgressBar pb = (ProgressBar) view;
		final int shot = cursor.getPosition();
		final int len = shotLength[shot];
		if (len > 0 || !cursor.isNull(videoCol)){
			pb.setIndeterminate(false);
			pb.setMax(Math.max(len, progress[shot]));
			pb.setProgress(progress[shot]);
		}else{
			if (progress[shot] > 0){
				pb.setIndeterminate(true);
			}else{
				pb.setIndeterminate(false);
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final ProgressBar pb = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        pb.setIndeterminate(false);
        return pb;
	}

}