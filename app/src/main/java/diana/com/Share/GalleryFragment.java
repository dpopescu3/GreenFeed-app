package diana.com.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.util.ArrayList;

import diana.com.Profile.AccountSettingsActivity;
import diana.com.Profile.ProfileActivity;
import diana.com.R;
import diana.com.Utilis.FilePath;
import diana.com.Utilis.FileSearch;
import diana.com.Utilis.GridImageAdapter;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private static final int NUM_GRID_COLUMNS = 3;
    private String mAppend = "file:/";
    private String mSelectedImage;
    private GridView gridView;
    private ImageView galleryImage;
    private Spinner directorySpinner;
    private ProgressBar mProgressBar;

    private ArrayList<String> directories;
    private ArrayList<String> filteredDirectories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        galleryImage = view.findViewById(R.id.galleryImageView);
        gridView = view.findViewById(R.id.gridView);
        directorySpinner = view.findViewById(R.id.spinnerDIRECTORY);
        mProgressBar = view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        directories = new ArrayList<>();
        filteredDirectories = new ArrayList<>();

        ImageView shareClone = view.findViewById(R.id.ivCloseShare);
        shareClone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
        TextView nextScreen = view.findViewById(R.id.tvNEXT);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRootTask()){
                Intent intent = new Intent(getActivity(), NextActivity.class);
                intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                startActivity(intent);}
                else {
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment),getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }

            }
        });

        init();

        return view;
    }
    private boolean isRootTask(){
        if(((ShareActivity)getActivity()).getTask()==0){
            return true;
        }else {
            return false;
        }
    }

    private void init() {
        FilePath filePath = new FilePath();
        ///check for other folders inside
        if (FileSearch.getDirectoryPath(filePath.PICTURES) == null) {
            return;
        }

        directories = FileSearch.getDirectoryPath(filePath.PICTURES);

        ArrayList<String> directoryNames = new ArrayList<>();
        for (int i = 0; i < directories.size(); i++) {

            String currentDirectory = directories.get(i);
            File directory = new File(currentDirectory);
            File[] contents = directory.listFiles();

            // the directory file is not really a directory
            // OR folder is empty
            if (contents == null || contents.length == 0) {
                continue;
            }

            boolean hasImages = false;
            for (File content : contents) {
                if (isAnImage(content.getPath())) {
                    hasImages = true;
                    break;
                }
            }

            if (!hasImages) continue;

            filteredDirectories.add(currentDirectory);
            int index = currentDirectory.lastIndexOf("/");
            String string = currentDirectory.substring(index).replace("/", "");
            directoryNames.add(string);
        }

        filteredDirectories.add(filePath.CAMERA);
        int index = filePath.CAMERA.lastIndexOf("/");
        String cameraDir = filePath.CAMERA.substring(index).replace("/", "");
        directoryNames.add(cameraDir);

        if (getActivity() == null) {
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);
        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                ///setup the image grid
                setupGridView(filteredDirectories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private boolean isAnImage(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        return options.outWidth > 0 && options.outHeight > 0;
    }

    private void setupGridView(String selectedDirectory) {
        Log.d(TAG, "setupGridView: directory chosen" + selectedDirectory);
        final ArrayList<String> imgURL = FileSearch.getFilePath(selectedDirectory);
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, mAppend, imgURL);
        gridView.setAdapter(adapter);
        try {
            setImage(imgURL.get(0),galleryImage,mAppend);
            mSelectedImage=imgURL.get(0);
        }catch (ArrayIndexOutOfBoundsException e){
            Log.d(TAG, "setupGridView:ArrayIndexOutOfBoundsException "+e.getMessage());
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                setImage(imgURL.get(position), galleryImage, mAppend);
                mSelectedImage = imgURL.get(position);

            }
        });

    }

    private void setImage(String ImgURL, ImageView image, String append) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + ImgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.INVISIBLE);

            }
        });
    }

}
