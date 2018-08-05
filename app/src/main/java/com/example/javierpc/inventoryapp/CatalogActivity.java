package com.example.javierpc.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.javierpc.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Displays list of products that were entered and stored in the app.
 * Displays list of products that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    /**
     * Adapter for the ListView
     */
    ProductCursorAdapter mCursorAdapter;
    Button sellButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        ListView productListView = findViewById(R.id.productlistview);
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);


        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProduct();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertProduct() {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PROD_NAME, "Sony Xperia ABCD");
        values.put(ProductEntry.COLUMN_PROD_PRICE, 10);
        values.put(ProductEntry.COLUMN_PROD_QUANTITY, 10);
        values.put(ProductEntry.COLUMN_SUPL_NAME, "Sony");
        values.put(ProductEntry.COLUMN_SUPL_PHONE_NUMBER, "+1703455675");

        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PROD_NAME,
                ProductEntry.COLUMN_PROD_PRICE,
                ProductEntry.COLUMN_PROD_QUANTITY
        };
        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteAllProduct() {
        if (ProductEntry.CONTENT_URI != null) {
            int mRowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);

            if (mRowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful) + ProductEntry.CONTENT_URI, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void productSeller(int id, int quantity) {
        if (quantity > 0) {
            quantity = quantity - 1;
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PROD_QUANTITY, quantity);
            Uri updatedProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
            int mRowsModified = getContentResolver().update(updatedProductUri, values, null, null);
            if (mRowsModified == 0) {
                Toast.makeText(this, getString(R.string.product_sold_out), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_successfull_selling), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.product_sold_out), Toast.LENGTH_SHORT).show();
        }
    }

    public void productDetailer(int id) {
        Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);
        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
        intent.setData(currentProductUri);
        startActivity(intent);
    }
}