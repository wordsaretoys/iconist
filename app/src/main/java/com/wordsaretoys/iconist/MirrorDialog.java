package com.wordsaretoys.iconist;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * image mirroring options popup
 */
public class MirrorDialog extends DialogFragment {

	/**
	 * create a popup with persistent arguments
	 */
	public static MirrorDialog create(boolean mirrorX, boolean mirrorY) {
		MirrorDialog dlg = new MirrorDialog();
		Bundle b = new Bundle();
		b.putBoolean("mirrorX", mirrorX);
		b.putBoolean("mirrorY", mirrorY);
		dlg.setArguments(b);
		return dlg;
	}

	@Override
	public Dialog onCreateDialog(Bundle state) {
		Dialog dlg = super.onCreateDialog(state);
		dlg.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dlg;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View v = inflater.inflate(R.layout.dialog_mirror, container, false);

        CheckBox checkMirrorX = (CheckBox) v.findViewById(R.id.checkMirrorX);
        CheckBox checkMirrorY = (CheckBox) v.findViewById(R.id.checkMirrorY);
        
        final MainActivity main = (MainActivity) getActivity();
        final Renderer renderer = MainActivity.renderQ.getRenderer();
        Bundle args = getArguments();
        
        checkMirrorX.setChecked(args.getBoolean("mirrorX"));
        checkMirrorX.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				renderer.mirrorX = isChecked;
				main.adapter.notifyDataSetChanged();
			}
        });
        
        checkMirrorY.setChecked(args.getBoolean("mirrorY"));
        checkMirrorY.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				renderer.mirrorY = isChecked;
				main.adapter.notifyDataSetChanged();
			}
        });

        return v;
    }
	
}
