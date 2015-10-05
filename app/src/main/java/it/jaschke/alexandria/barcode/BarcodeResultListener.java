package it.jaschke.alexandria.barcode;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by GIL on 22/09/2015 for alexandria.
 */
public interface BarcodeResultListener {
    void onBarcodeResult(final Barcode barcode);
}
