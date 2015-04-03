package com.wordsaretoys.iconist;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioGroup;

/**
 * colors options popup
 */
public class ColorsDialog extends DialogFragment {

	static int[] colorChecks = {
		0, R.id.radio1Colors, R.id.radio2Colors, 
		R.id.radio3Colors, R.id.radio4Colors
	};
	
	/**
	 * create a popup with persistent arguments
	 */
	public static ColorsDialog create(int colors) {
		ColorsDialog dlg = new ColorsDialog();
		Bundle b = new Bundle();
		b.putInt("colors", colors);
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
        View v = inflater.inflate(R.layout.dialog_colors, container, false);

        RadioGroup optionColors = (RadioGroup) v.findViewById(R.id.optionColors);
        final MainActivity main = (MainActivity) getActivity();
        final Renderer renderer = MainActivity.renderQ.getRenderer();
        Bundle args = getArguments();
        
        optionColors.check(colorChecks[args.getInt("colors")]);
        optionColors.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
				case R.id.radio1Colors:
					renderer.colorCount = 1;
					break;
				case R.id.radio2Colors:
					renderer.colorCount = 2;
					break;
				case R.id.radio3Colors:
					renderer.colorCount = 3;
					break;
				case R.id.radio4Colors:
					renderer.colorCount = 4;
					break;
				}
				main.adapter.notifyDataSetChanged();
			}
        });

        return v;
    }		
}
