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
 * detail options popup
 */
public class DetailDialog extends DialogFragment {

	static int[] detailChecks = {
		0, R.id.radio1Detail, R.id.radio2Detail, 
		R.id.radio3Detail, R.id.radio4Detail
	};
	
	/**
	 * create a popup with persistent arguments
	 */
	public static DetailDialog create(int detail) {
		DetailDialog dlg = new DetailDialog();
		Bundle b = new Bundle();
		b.putInt("detail", detail);
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
        View v = inflater.inflate(R.layout.dialog_detail, container, false);

        RadioGroup optionDetail = (RadioGroup) v.findViewById(R.id.optionDetail);
        final MainActivity main = (MainActivity) getActivity();
        final Renderer renderer = MainActivity.renderQ.getRenderer();
        Bundle args = getArguments();
        
        optionDetail.check(detailChecks[args.getInt("detail")]);
        optionDetail.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
				case R.id.radio1Detail:
					renderer.detail = 1;
					break;
				case R.id.radio2Detail:
					renderer.detail= 2;
					break;
				case R.id.radio3Detail:
					renderer.detail= 3;
					break;
				case R.id.radio4Detail:
					renderer.detail= 4;
					break;
				}
				main.adapter.notifyDataSetChanged();
			}
        });

        return v;
    }		
}
