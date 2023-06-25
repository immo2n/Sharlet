package com.moonslab.sharlet;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class File_selection_grid_adapter extends BaseAdapter {
    Context context;
    File[] files;
    LayoutInflater inflater;
    private DBHandler dbHandler;

    public File_selection_grid_adapter(Context context, File[] file_target) {
        this.context = context;
        this.files = file_target;
    }

    @Override
    public int getCount() {
        return files.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        dbHandler = new DBHandler(context);
        if(inflater == null){
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if(convertView == null){
            convertView = inflater.inflate(R.layout.image_grid, null);
        }
        ImageView image = convertView.findViewById(R.id.grid_image);
        TextView name = convertView.findViewById(R.id.file_name);
        File target_file = files[position];
        name.setText(target_file.getName());

        //Image test
        String File_name = target_file.getName();
        String File_extension = FilenameUtils.getExtension(File_name).toLowerCase(Locale.ROOT);
        if(null == File_extension || File_extension.isEmpty()){
            convertView = inflater.inflate(R.layout.unknown_grid, null);
            TextView name_x = convertView.findViewById(R.id.file_name2);
            name_x.setText(File_name);
        }
        //Image
        if (File_extension.equals("png")
                || File_extension.equals("jpg")
                || File_extension.equals("gif")
                || File_extension.equals("jpeg")
                || File_extension.equals("heic")
                || File_extension.equals("webp")
                || File_extension.equals("tiff")
                || File_extension.equals("raw")) {
            convertView.setOnLongClickListener(v -> {
                Intent intent = new Intent(context, Photo_view.class);
                //Save the file first
                store_as_file("Image_last.txt", target_file.getPath(), context);
                context.startActivity(intent);
                return true;
            });
            if (null != image) {
                try {
                    Picasso.get().load(target_file).placeholder(R.drawable.ic_baseline_photo_24).resize(250, 250).centerCrop().into(image);
                } catch (Exception e) {
                    Picasso.get().load(R.drawable.ic_baseline_photo_24).resize(250, 250).centerCrop().into(image);
                }
            }
        }
        else if (File_extension.equals("mp3")
                || File_extension.equals("wav")
                || File_extension.equals("ogg")
                || File_extension.equals("m4a")
                || File_extension.equals("aac")
                || File_extension.equals("alac")
                || File_extension.equals("aiff")){
            convertView.setOnLongClickListener(v -> {
                Intent intent = new Intent(context, Music_player.class);
                //Save the file first
                //Unset loop
                Intent in = Home.get_music_intent(dbHandler, target_file.getPath(), context);
                in.setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(in);
                return true;
            });
            if(null != image) {
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(target_file.getPath());
                    byte[] data = mmr.getEmbeddedPicture();
                    if (data != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        image.setImageBitmap(bitmap);
                    } else {
                        image.setImageResource(R.drawable.ic_baseline_audio_file_24);
                    }
                } catch (Exception e) {
                    image.setImageResource(R.drawable.ic_baseline_audio_file_24);
                }
            }

        }
        else if (File_extension.equals("mp4")
                || File_extension.equals("mkv")
                || File_extension.equals("flv")
                || File_extension.equals("avi")
                || File_extension.equals("webm")
                || File_extension.equals("3gp")
                || File_extension.equals("mov")) {
            convertView.setOnLongClickListener(v -> {
                Intent intent = new Intent(context, Video_player.class);
                store_as_file("Video_last.txt", target_file.getPath(), context);
                context.startActivity(intent);
                return true;
            });
            if(null != image) {
                try {
                    Glide.with(context)
                            .load(target_file)
                            .placeholder(R.drawable.ic_baseline_video_file_24)
                            .into(image);
                } catch (Exception e) {
                    Picasso.get().load(R.drawable.ic_baseline_video_file_24).resize(250, 250).centerCrop().into(image);
                }
            }
        }
        else {
            View convertView2 = inflater.inflate(R.layout.unknown_grid, null);
            TextView ext_holder = convertView2.findViewById(R.id.unknown_file_text);
            if(File_extension.length() > 3){
                File_extension = File_extension.substring(0, 3);
            }
            ext_holder.setText(File_extension);
            TextView name_x = convertView2.findViewById(R.id.file_name2);
            name_x.setText(File_name);
            convertView2.setOnLongClickListener(v -> {
                Toast.makeText(context, "Can't view this file!", Toast.LENGTH_SHORT).show();
                return true;
            });
            convertView = convertView2;
        }

        //Set hold listener to make it selected
        View finalConvertView = convertView;
        convertView.setOnClickListener(v -> {
            //Read the bucket
            List<String> bucket_list = new ArrayList<>();
            String location = Home.get_app_home_bundle_data_store()+"/Selection_bucket.txt";
            File main_file = new File(location);
            if(!main_file.exists()){
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(main_file));
                    outputStreamWriter.write("");
                    outputStreamWriter.close();
                }
                catch (Exception e){
                    Toast.makeText(context, "Can't select!", Toast.LENGTH_SHORT).show();
                }
            }
            try {
                BufferedReader reader  = new BufferedReader(new FileReader(main_file));
                String line = reader.readLine();
                while (line != null) {
                    bucket_list.add(line);
                    // read next line
                    line = reader.readLine();
                }
                reader.close();
            }
            catch (IOException e){
                bucket_list = null;
            }

            Boolean selected_already = false;
                //Not null, so look for the file
                //if exists, its selected
                for (String path : bucket_list) {
                    String path0 = target_file.getPath();
                    String home = "/storage";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        home = Environment.getStorageDirectory().getPath();
                    }
                    path0 = path0.substring(home.length());
                    if (path.equals(path0)) {
                        selected_already = true;
                        break;
                    }
            }

            LinearLayout main_layout = finalConvertView.findViewById(R.id.main_layout);

            if(!selected_already) {
                main_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.selection));
                //Add to the list convert to text and save
                String path0 = target_file.getPath();
                String home = "/storage";
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    home = Environment.getStorageDirectory().getPath();
                }
                path0 = path0.substring(home.length());
                bucket_list.add(path0);
                //New bucket
                String new_data = null;
                for(String path : bucket_list){
                    if(path.equals("empty")){
                        continue;
                    }
                    if(null == new_data){
                        new_data = path;
                    }
                    else {
                        new_data+= System.lineSeparator()+path;
                    }
                }
                if(null != new_data){
                    //Save the string
                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(main_file));
                        outputStreamWriter.write(new_data);
                        outputStreamWriter.close();
                    }
                    catch (Exception e){
                        Toast.makeText(context, "Can't select!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else {
                main_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                if(bucket_list.size() == 1){
                    //Empty and return
                    main_file.delete();
                    File_selection.selection_update();
                    return;
                }
                //New bucket
                String new_data = null;
                for(String path : bucket_list){
                    String path0 = target_file.getPath();
                    String home = "/storage";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        home = Environment.getStorageDirectory().getPath();
                    }
                    path0 = path0.substring(home.length());
                    if(path.equals("empty") || path.equals(path0)){
                        continue;
                    }
                    if(null == new_data){
                        new_data = path;
                    }
                    else {
                        new_data+= System.lineSeparator()+path;
                    }
                }
                if(null != new_data){
                    //Save the string
                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(main_file));
                        outputStreamWriter.write(new_data);
                        outputStreamWriter.close();
                    }
                    catch (Exception e){
                        Toast.makeText(context, "Can't unselect!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            File_selection.selection_update();
        });
        return convertView;

        //Unlikely this will reach
    }
    //Helpers
    private void store_as_file(String file_name, String data , Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file_name, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Toast.makeText(context, "Error loading file!", Toast.LENGTH_SHORT).show();
        }
    }
}