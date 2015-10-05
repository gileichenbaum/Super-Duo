package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

import it.jaschke.alexandria.barcode.BarcodeScanActivity;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ViewTreeObserver.OnGlobalLayoutListener {

    private static final int REQUEST_BARCODE_SCAN = 5432;
    private final int LOADER_ID = 1;
    private final String EAN_CONTENT = "eanContent";

    private EditText ean;
    private View rootView;

    private TextView mBookTitleTxt;
    private Window mRootWindow;
    private View mRootView;
    private Rect mDisplayRect = new Rect();
    private int mScreenHeight;
    private int mSnackbarBottomMargin;
    private Snackbar mSnackbar;
    private View mScanBtn;
    private boolean mIsOnBackground;
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //no need
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //no need
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!mIsOnBackground) {
                startSearch(s);
            }
        }
    };

    public AddBook() {
    }

    @Override
    public void onPause() {
        mIsOnBackground = true;
        super.onPause();
    }

    @Override
    public void onResume() {
        mIsOnBackground = false;
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ean != null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        mScanBtn.setEnabled(true);

        if (requestCode == REQUEST_BARCODE_SCAN && resultCode == Activity.RESULT_OK && data != null && data.hasExtra(BarcodeScanActivity.BARCODE)) {
            final Barcode barcode = data.getParcelableExtra(BarcodeScanActivity.BARCODE);
            if (barcode != null) {
                ean.setText(barcode.displayValue);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        init(context);
    }

    private void init(final Context context) {
        final FragmentActivity activity = getActivity();
        activity.setTitle(R.string.scan);
        mRootWindow = activity.getWindow();
        mRootView = mRootWindow.getDecorView().findViewById(android.R.id.content);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mScreenHeight = context.getResources().getDisplayMetrics().heightPixels;
        mSnackbar = Snackbar.make(mRootView, R.string.clear_prev_search, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFields();
                startSearch(ean.getText());
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);

        mBookTitleTxt = (TextView) rootView.findViewById(R.id.bookTitle);
        ean.addTextChangedListener(mTextWatcher);

        mScanBtn = rootView.findViewById(R.id.scan_button);
        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                startActivityForResult(new Intent(getActivity(), BarcodeScanActivity.class), REQUEST_BARCODE_SCAN);
            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFields();
                ean.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                clearFields();
                ean.setText("");
            }
        });

        if (savedInstanceState != null) {
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (ean != null) {
            ean.removeTextChangedListener(mTextWatcher);
        }
        super.onDestroyView();
    }

    private void startSearch(final Editable s) {
        String ean = s.toString();

        if (mSnackbar.isShown()) {
            return;
        } else if (TextUtils.isEmpty(mBookTitleTxt.getText())) {
            clearFields();
        } else {
            final View snackbarView = mSnackbar.getView();
            final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) snackbarView.getLayoutParams();
            layoutParams.bottomMargin = mSnackbarBottomMargin;
            mSnackbar.show();
            return;
        }

        //catch isbn10 numbers
        if (ean.length() == 10 && !ean.startsWith("978")) {
            ean = "978" + ean;
        }

        //Once we have an ISBN, start a book intent
        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EAN, ean);
        bookIntent.setAction(BookService.FETCH_BOOK);
        getActivity().startService(bookIntent);
        AddBook.this.restartLoader();
    }

    private void restartLoader() {
        final LoaderManager loaderManager = getLoaderManager();
        if (loaderManager != null && loaderManager.hasRunningLoaders()) {
            loaderManager.restartLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (ean.getText().length() == 0) {
            return null;
        }
        String eanStr = ean.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
            eanStr = "978" + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst() || mSnackbar.isShown()) {
            return;
        }

        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        mBookTitleTxt.setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (!TextUtils.isEmpty(authors)) {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        }

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (!TextUtils.isEmpty(imgUrl) && Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields() {
        mBookTitleTxt.setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        init(activity);
    }

    @Override
    public void onGlobalLayout() {
        View view = mRootWindow.getDecorView();
        view.getWindowVisibleDisplayFrame(mDisplayRect);
        mSnackbarBottomMargin = mScreenHeight - mDisplayRect.bottom;
    }
}
