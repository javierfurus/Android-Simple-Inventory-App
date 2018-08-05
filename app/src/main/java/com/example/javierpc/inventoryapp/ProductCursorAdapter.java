package com.example.javierpc.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.javierpc.inventoryapp.data.ProductContract.ProductEntry;


public class ProductCursorAdapter extends CursorAdapter {
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView tvName = view.findViewById(R.id.name);
        TextView tvPrice = view.findViewById(R.id.price);
        TextView tvQuantity = view.findViewById(R.id.quantity);
        Button sellButton = view.findViewById(R.id.sell_button);
        Button detailsButton = view.findViewById(R.id.details_button);

        final String productID = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry._ID));
        final String name = cursor.getString(cursor.getColumnIndexOrThrow("productname"));
        final String price = cursor.getString(cursor.getColumnIndexOrThrow("price"));
        final String quantity = cursor.getString(cursor.getColumnIndexOrThrow("quantity"));

        tvName.setText(name);
        tvPrice.setText(price);
        tvQuantity.setText(quantity);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CatalogActivity Catalog = (CatalogActivity) context;
                Catalog.productSeller(Integer.valueOf(productID), Integer.valueOf(quantity));
            }
        });
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CatalogActivity Catalog = (CatalogActivity) context;
                Catalog.productDetailer(Integer.valueOf(productID));
            }
        });
    }
}
