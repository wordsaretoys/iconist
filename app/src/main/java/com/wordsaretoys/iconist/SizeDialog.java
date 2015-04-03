package com.wordsaretoys.iconist;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * image size popup
 */
public class SizeDialog extends DialogFragment {

	/**
	 * create a popup with persistent arguments
	 */
	public static SizeDialog create(int width, int height) {
		SizeDialog dlg = new SizeDialog();
		Bundle b = new Bundle();
		b.putInt("width", width);
		b.putInt("height", height);
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
        View v = inflater.inflate(R.layout.dialog_size, container, false);

        final EditText widthBox = (EditText) v.findViewById(R.id.sizeX);
        final EditText heightBox = (EditText) v.findViewById(R.id.sizeY);
        Button setButton = (Button) v.findViewById(R.id.sizeSet);

        final MainActivity main = (MainActivity) getActivity();
        Bundle args = getArguments();
        
        widthBox.setText("" + args.getInt("width"));
        heightBox.setText("" + args.getInt("height"));
        
        setButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String ws = widthBox.getText().toString();
				int width = Integer.valueOf(ws.isEmpty() ? "0" : ws);
				String hs = heightBox.getText().toString();
				int height = Integer.valueOf(hs.isEmpty() ? "0" : hs);
				
				if (width < 32 || height < 32) {
					// either width or height is too small
					Toast
					.makeText(getActivity(), 
						R.string.optionSizeTooSmall, 
						Toast.LENGTH_SHORT)
					.show();
				} else if (width > 2048 || height > 2048) {
					// either width or height is too large
					Toast
					.makeText(getActivity(), 
						R.string.optionSizeTooLarge, 
						Toast.LENGTH_SHORT)
					.show();
				} else if (Math.max(width / height, height / width) > 8) {
					// aspect ratio is too large
					Toast
					.makeText(getActivity(), 
						R.string.optionSizeInvalid, 
						Toast.LENGTH_SHORT)
					.show();
				} else {
					// just right, set it
					main.setImageSize(width, height);
				}
			}
        });
        
        return v;
    }

}
