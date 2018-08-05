package com.example.javierpc.inventoryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.javierpc.inventoryapp.data.ProductContract;
import com.example.javierpc.inventoryapp.data.ProductDbHelper;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_PHONE_CALL = 1;
    private static final int EXISTING_PRODUCT_LOADER = 0;
    public ProductDbHelper mDbHelper;
    private TextView mNameTv;
    private TextView mPriceTv;
    private TextView mQuantityTv;
    private TextView mPhoneNumberTv;
    private TextView mSupplierTv;
    private Button mAddButton;
    private Button mRemoveButton;
    private ImageButton callButton;
    private int quantity;
    private int phoneNumber;
    private Uri currentProductUri;

    private View.OnClickListener mClickListenerAdd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            productIncreaser(currentProductUri);
        }
    };

    private View.OnClickListener mClickListenerRemove = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            productReducer(currentProductUri);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        currentProductUri = intent.getData();
        setTitle(getString(R.string.activity_title_details));
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mNameTv = findViewById(R.id.edit_product_name);
        mPriceTv = findViewById(R.id.edit_product_price);
        mQuantityTv = findViewById(R.id.edit_product_quantity);
        mPhoneNumberTv = findViewById(R.id.edit_supplier_phone_number);
        mSupplierTv = findViewById(R.id.spinner_supplier);
        mAddButton = findViewById(R.id.button_increase);
        mRemoveButton = findViewById(R.id.button_reduce);
        mDbHelper = new ProductDbHelper(this);
        mAddButton.setOnClickListener(mClickListenerAdd);
        mRemoveButton.setOnClickListener(mClickListenerRemove);
        callButton = findViewById(R.id.call_supplier_button);

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supplierCaller();
            }
        });

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        int mRowsDeleted = getContentResolver().delete(currentProductUri, null, null);

        if (mRowsDeleted == 0) {
            Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_delete_product_successful) + currentProductUri, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PROD_NAME,
                ProductContract.ProductEntry.COLUMN_PROD_PRICE,
                ProductContract.ProductEntry.COLUMN_PROD_QUANTITY,
                ProductContract.ProductEntry.COLUMN_SUPL_NAME,
                ProductContract.ProductEntry.COLUMN_SUPL_PHONE_NUMBER,
        };
        return new CursorLoader(this,
                currentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }
        if (data.moveToFirst()) {

            // Figure out the index of each column
            int prodNameColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PROD_NAME);
            int prodPriceColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PROD_PRICE);
            int prodQuantityColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PROD_QUANTITY);
            int suplNameColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPL_NAME);
            int suplPhoneColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPL_PHONE_NUMBER);

            String currentProdName = data.getString(prodNameColumnIndex);
            int currentQuantity = data.getInt(prodQuantityColumnIndex);
            int currentProductPrice = data.getInt(prodPriceColumnIndex);
            String currentSuplName = data.getString(suplNameColumnIndex);
            int currentSuplPhone = data.getInt(suplPhoneColumnIndex);
            //Set the EditText fields
            mNameTv.setText(currentProdName);
            mPhoneNumberTv.setText(Integer.toString(currentSuplPhone));
            mPriceTv.setText(Integer.toString(currentProductPrice));
            mQuantityTv.setText(Integer.toString(currentQuantity));
            quantity = currentQuantity;
            phoneNumber = currentSuplPhone;
            mSupplierTv.setText(currentSuplName);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameTv.setText("");
        mPhoneNumberTv.setText("");
        mPriceTv.setText("");
        mQuantityTv.setText("");
        mSupplierTv.setText("");
    }

    @Override
    public void onBackPressed() {

    }


    public void productReducer(Uri currentProductUri) {
        if (quantity > 0) {
            quantity = quantity - 1;
            ContentValues values = new ContentValues();
            values.put(ProductContract.ProductEntry.COLUMN_PROD_QUANTITY, quantity);
            getContentResolver().update(currentProductUri, values, null, null);
        }
    }

    public void productIncreaser(Uri currentProductUri) {
        quantity = quantity + 1;
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PROD_QUANTITY, quantity);
        getContentResolver().update(currentProductUri, values, null, null);
    }

    public void supplierCaller() {

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        if (ContextCompat.checkSelfPermission(DetailsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DetailsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
        } else {
            if (intent.resolveActivity(getPackageManager()) != null) {

                startActivity(intent);  //where intent is your intent

            }
        }
    }
}