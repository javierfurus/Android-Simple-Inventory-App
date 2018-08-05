/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.javierpc.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.javierpc.inventoryapp.data.ProductContract.ProductEntry;
import com.example.javierpc.inventoryapp.data.ProductDbHelper;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    public ProductDbHelper mDbHelper;
    ProductCursorAdapter mCursorAdapter;
    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the product's price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the product's quantity
     */
    private EditText mQuantitytEditText;
    /**
     * EditText field to enter the product's supplier's phone number
     */
    private EditText mPhoneNumberEditText;
    /**
     * EditText field to enter the product's supplier
     */
    private Spinner mSupplierSpinner;
    /**
     * Supplier of the product. The possible values are:
     * 0 for unknown supplier, 1 for male, 2 for female.
     */
    private String mSupplier;
    private Uri currentProductUri;
    private boolean productHasChanged;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        /** Identifier for the product data loader */
        Intent intent = getIntent();
        currentProductUri = intent.getData();
        if (currentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_product_name);
        mPriceEditText = findViewById(R.id.edit_product_price);
        mQuantitytEditText = findViewById(R.id.edit_product_quantity);
        mPhoneNumberEditText = findViewById(R.id.edit_supplier_phone_number);
        mSupplierSpinner = findViewById(R.id.spinner_supplier);
        mDbHelper = new ProductDbHelper(this);
        productHasChanged = false;

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantitytEditText.setOnTouchListener(mTouchListener);
        mPhoneNumberEditText.setOnTouchListener(mTouchListener);
        mSupplierSpinner.setOnTouchListener(mTouchListener);
        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the supplier of the product.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter supplierSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSupplierSpinner.setAdapter(supplierSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_apple))) {
                        mSupplier = ProductEntry.SUPPLIER_APPLE; // Apple
                    } else if (selection.equals(getString(R.string.supplier_sony))) {
                        mSupplier = ProductEntry.SUPPLIER_SONY; // Sony
                    } else {
                        mSupplier = ProductEntry.SUPPLIER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = ProductEntry.SUPPLIER_UNKNOWN; // Unknown
            }
        });
    }

    private void saveProduct() {
        int quantity = 0;
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantitytEditText.getText().toString().trim();
        if (quantityString.equals("")) {
            quantity = 0;
        } else {
            quantity = Integer.parseInt(quantityString);
        }
        String phoneNumberString = mPhoneNumberEditText.getText().toString().trim();

        if (currentProductUri == null && TextUtils.isEmpty(nameString)
                || TextUtils.isEmpty(priceString)
                || quantityString.equals("")
                || mSupplier.equals(ProductEntry.SUPPLIER_UNKNOWN)) {
            Toast.makeText(this, getString(R.string.error_while_empty_editor), Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PROD_NAME, nameString);
        values.put(ProductEntry.COLUMN_PROD_PRICE, priceString);
        values.put(ProductEntry.COLUMN_PROD_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_SUPL_NAME, mSupplier);
        values.put(ProductEntry.COLUMN_SUPL_PHONE_NUMBER, phoneNumberString);

        if (currentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.error_while_saving), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.success_while_saving) + newUri, Toast.LENGTH_SHORT).show();
            }

        } else {
            int mRowsUpdated = getContentResolver().update(currentProductUri, values, null, null);

            if (mRowsUpdated == 0) {
                Toast.makeText(this, getString(R.string.error_while_saving), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.success_while_saving) + currentProductUri, Toast.LENGTH_SHORT).show();
            }
        }
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
        if (currentProductUri != null) {
            int mRowsDeleted = getContentResolver().delete(currentProductUri, null, null);

            if (mRowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful) + currentProductUri, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
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
                ProductEntry._ID,
                ProductEntry.COLUMN_PROD_NAME,
                ProductEntry.COLUMN_PROD_PRICE,
                ProductEntry.COLUMN_PROD_QUANTITY,
                ProductEntry.COLUMN_SUPL_NAME,
                ProductEntry.COLUMN_SUPL_PHONE_NUMBER,
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
            int prodNameColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PROD_NAME);
            int prodPriceColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PROD_PRICE);
            int prodQuantityColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PROD_QUANTITY);
            int suplNameColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_SUPL_NAME);
            int suplPhoneColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_SUPL_PHONE_NUMBER);

            String currentProdName = data.getString(prodNameColumnIndex);
            int currentQuantity = data.getInt(prodQuantityColumnIndex);
            int currentProductPrice = data.getInt(prodPriceColumnIndex);
            String currentSuplName = data.getString(suplNameColumnIndex);
            int currentSuplPhone = data.getInt(suplPhoneColumnIndex);
            //Set the EditText fields
            mNameEditText.setText(currentProdName);
            mPhoneNumberEditText.setText(Integer.toString(currentSuplPhone));
            mPriceEditText.setText(Integer.toString(currentProductPrice));
            mQuantitytEditText.setText(Integer.toString(currentQuantity));

            switch (currentSuplName) {
                case ProductEntry.SUPPLIER_APPLE:
                    mSupplierSpinner.setSelection(1);
                    break;
                case ProductEntry.SUPPLIER_SONY:
                    mSupplierSpinner.setSelection(2);
                    break;
                default:
                    mSupplierSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPhoneNumberEditText.setText("");
        mPriceEditText.setText("");
        mQuantitytEditText.setText("");
        mSupplierSpinner.setSelection(0);
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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
}