package by.jeffset.layncher;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import by.jeffset.layncher.data.PhonesContract;

public class PhonesListAdapter extends RecyclerViewCursorAdapter<PhonesListAdapter.PhoneViewHolder> {
   private int nameColInd;
   private int numberColInd;
   private int imageColInd;
   private int idColInd;

   private IconDataFragment dataFragment;
   private Activity activity;
   PhoneViewListener listener = null;

   public interface PhoneViewListener {
      void onPhoneViewClick(String name, String number);

      boolean onPhoneViewLongClick(String name, String number, Drawable icon, long contactID);
   }

   class PhoneViewHolder extends RecyclerView.ViewHolder {
      TextView labelView;
      ImageView iconView;

      PhoneViewHolder(View itemView) {
         super(itemView);
         itemView.setOnClickListener(v -> {
            if (listener != null) {
               Cursor cursor = getCursor();
               if (!cursor.moveToPosition(getAdapterPosition()))
                  throw new IllegalStateException();
               listener.onPhoneViewClick(
                   cursor.getString(nameColInd),
                   cursor.getString(numberColInd));
            }
         });
         itemView.setOnLongClickListener(v -> {
            if (listener != null) {
               Cursor cursor = getCursor();
               if (!cursor.moveToPosition(getAdapterPosition()))
                  throw new IllegalStateException();
               return listener.onPhoneViewLongClick(
                   cursor.getString(nameColInd),
                   cursor.getString(numberColInd),
                   iconView.getDrawable(),
                   cursor.getLong(idColInd));
            }
            return false;
         });
         labelView = (TextView) itemView.findViewById(android.R.id.text1);
         iconView = (ImageView) itemView.findViewById(android.R.id.icon);
         ImageView miniImageView = (ImageView) itemView.findViewById(android.R.id.icon1);
         miniImageView.setImageResource(R.drawable.ic_phone_white_36px);
      }

      void bind(Cursor cursor) {
         labelView.setText(cursor.getString(nameColInd));
         byte[] blob = cursor.getBlob(imageColInd);
         if (blob != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            iconView.setImageBitmap(bitmap);
         } else
            iconView.setImageResource(R.drawable.ic_person_white_48px);
      }
   }

   public PhonesListAdapter(@NonNull Activity activity, @NonNull Cursor cursor) {
      super(cursor);
      this.activity = activity;
      nameColInd = cursor.getColumnIndex(PhonesContract.Contact.NAME);
      numberColInd = cursor.getColumnIndex(PhonesContract.Contact.NUMBER);
      imageColInd = cursor.getColumnIndex(PhonesContract.Contact.IMAGE);
      idColInd = cursor.getColumnIndex(PhonesContract.Contact.CONTACT_ID);
   }

   @Override public void onBindViewHolder(PhoneViewHolder holder, Cursor cursor) {
      holder.bind(cursor);
   }

   @Override public PhoneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_icon, null);
      return new PhoneViewHolder(v);
   }
}
