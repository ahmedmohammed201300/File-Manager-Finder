package info.androidhive.navigationdrawer.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.androidhive.navigationdrawer.R;
import info.androidhive.navigationdrawer.adapters.ListViewAdapterImages;
import info.androidhive.navigationdrawer.db.DbController;
import info.androidhive.navigationdrawer.model.FileUtils;
import info.androidhive.navigationdrawer.model.LstViewImageItem;

/**
 * Created by ahmed on 16/08/17.
 */

public class FragmentImages extends Fragment implements ListView.OnItemClickListener {

    View viewRoot = null;
    Context context = null;
    @BindView(R.id.lvFragImages)
    ListView listView;
    ListViewAdapterImages listViewAdapterImages = null;
    ArrayList<LstViewImageItem> listImages = new ArrayList<LstViewImageItem>();
    ArrayList<File> listFiles = new ArrayList<File>();
    List<String> tFileList = new ArrayList<String>();
    private String SD_CARD_ROOT;
    @BindView(R.id.fragImages)
    RelativeLayout relativeLayout;
    int lstOrGridVisibileNow = 0;//default listview
    FloatingActionButton fabMenuGreen = null;
    DbController dbController = null;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        viewRoot = inflater.inflate(R.layout.fragment_images, container, false);
        context = getActivity();
        context = container.getContext();
        context = getContext();

        setHasOptionsMenu(true);
        dbController = new DbController(context);
        ButterKnife.bind(this, viewRoot);

        //relativeLayout.addView(inflateFabMenuGreen(context));
        //fireToast("images fragment");

        //getting the path of external sd card for getting all images from there
        File mFile = Environment.getExternalStorageDirectory();
        SD_CARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
        SD_CARD_ROOT = mFile.toString();

        //getting all images by executing the async task

        new AsyncTastGetAllImages(context).execute(SD_CARD_ROOT);
        listViewAdapterImages = new ListViewAdapterImages(context, listImages, 0, false);
        listView.setAdapter(listViewAdapterImages);
        listView.setOnItemClickListener(this);
        listViewAdapterImages.notifyDataSetChanged();

        return viewRoot;
    }

    public View inflateFabMenuGreen(Context context) {
        View button = LayoutInflater.from(context).inflate(R.layout.fab_menu_green, null);
        button.findViewById(R.id.fabCreateFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFile();
            }
        });
        button.findViewById(R.id.fabCreateFolder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDirec();
            }
        });
        return button;
    }

    private void addDirec() {

    }

    private void addFile() {

    }

    private void fireToast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    // for checking if a filepath is an image, would be:
    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public String setFormatForDate(Date date) {
        String DATE_FORMAT = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        System.out.println("Formated Date " + sdf.format(date));
        return sdf.format(date);
    }

    public Date getCreationDateForFile(String filePath) {
        File file = new File(filePath);
        Date lastModDate = new Date(file.lastModified());
        //Log.i("creation", "File last modified @ : "+ lastModDate.toString());
        //return lastModDate.toString();
        return lastModDate;
    }

    public List<String> findFiles() {

        Resources resources = getActivity().getResources();
        // array of valid image file extensions
        String[] imageTypes = resources.getStringArray(R.array.image);

        FilenameFilter[] filter = new FilenameFilter[imageTypes.length];
        int i = 0;
        for (final String type : imageTypes) {
            filter[i] = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith("." + type);
                }
            };
            i++;
        }

        FileUtils fileUtils = new FileUtils();
        File[] allMatchingFiles = fileUtils.listFilesAsArray(new File(SD_CARD_ROOT), filter, -1);
        for (File f : allMatchingFiles) {
            tFileList.add(f.getAbsolutePath());
            listFiles.add(f);
        }

        //Log.i("paths", tFileList.toString());

        return tFileList;
    }

    class AsyncTastGetAllImages extends AsyncTask<String, Void, ArrayList<LstViewImageItem>> {

        Context context = null;
        ProgressDialog dialog = null;


        AsyncTastGetAllImages(Context context) {
            this.context = context;
            dialog = new ProgressDialog(this.context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setTitle("Please Wait");
            dialog.setCancelable(false);
            dialog.setMessage("Getting all images ... ");
            dialog.setIndeterminate(true);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!dialog.isShowing()) {
                dialog.show();
            }

        }

        @Override
        protected ArrayList<LstViewImageItem> doInBackground(String... params) {
            findFiles();
            for (int i = 0; i < listFiles.size(); i++) {
                File file = listFiles.get(i);
                String imgName = "";
                Date imgCreationDate = null;
                String imgSize = "";
                String imgPath = "";

                imgName = file.getName();//image name associated with extention
//            imgSize = file.getTotalSpace()+"";//image size in kb
                double bytes = file.length();
                double kilobytes = (bytes / 1024);
                NumberFormat formatter = new DecimalFormat("#0.00");
                imgSize = formatter.format(kilobytes);
                imgCreationDate = getCreationDateForFile(file.getAbsolutePath());//image creation date or last modification date
                imgPath = tFileList.get(i);//image uri

                LstViewImageItem lstViewImageItem = new
                        LstViewImageItem(imgPath, imgName, setFormatForDate(imgCreationDate), imgSize);
                listImages.add(lstViewImageItem);

            }

            return listImages;
        }

        @Override
        protected void onPostExecute(ArrayList<LstViewImageItem> lstViewImageItems) {
            super.onPostExecute(lstViewImageItems);

            listImages = lstViewImageItems;

            if (dialog.isShowing())
                dialog.dismiss();

            if (!lstViewImageItems.isEmpty() && !lstViewImageItems.equals(null)) {
                listViewAdapterImages.notifyDataSetChanged();
            }


        }
    }

    LstViewImageItem item = null;
    File file = null;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        item = listImages.get(position);
        file = listFiles.get(position);
        //--------------------------------------------------------------
        setPopUpMenu(view);
        //-------------------------------------------------------
        showTheFileUsingExistingApps();

    }

    private void showTheFileUsingExistingApps() {
        Uri uri =  Uri.fromFile(file);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        String mime = "*/*";
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (mimeTypeMap.hasExtension(
                mimeTypeMap.getFileExtensionFromUrl(uri.toString())))
            mime = mimeTypeMap.getMimeTypeFromExtension(
                    mimeTypeMap.getFileExtensionFromUrl(uri.toString()));
        intent.setDataAndType(uri,mime);
        startActivity(intent);
    }


    private void setPopUpMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_for_one_folder, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.folderAddToBookmark:
                        //fireToast("add to bookmark");
                        fileAddToBookMark();
                        break;
                    case R.id.folderCopy:
                        //fireToast("copy");
                        fileCopy();
                        break;
                    case R.id.folderCut:
                        fileCut();
                        break;
                    case R.id.folderRename:
                        fileRename();
                        break;
                    case R.id.folderProperties:
                        break;
                    case R.id.folderDelete:
                        fileDelete();
                        break;
                    default:
                        break;

                }
                //--------------------------------------

                return true;
            }
        });
        popupMenu.show();
    }

    private void fileDelete() {

        try {
            if (file.delete()) {
                fireToast(file.getName() + " deleted");
            } else {

            }
        } catch (Exception e) {
            fireToast("Error occurred");
            e.printStackTrace();
        }

    }

    private void fileCopy() {

    }

    private void fileCut() {
    }

    private void fileAddToBookMark() {

    }



    AlertDialog alertDialog = null;
    AlertDialog.Builder builder = null;

    private void fileRename() {

        View view = LayoutInflater.from(context).inflate(R.layout.file_rename, null);
        EditText etOldName = (EditText) view.findViewById(R.id.etOldFileName);
        final EditText etNewName = (EditText) view.findViewById(R.id.etNewFileName);

        etOldName.setText(item.getImgName());


        builder = new AlertDialog.Builder(context);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Rename", null);
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPos, btnNeg;
                btnPos = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Log.e("ext", getFileExt(file.getName()));
                        if (etNewName.getText().toString().trim().length() == 0) {
                            etNewName.requestFocus();
                            fireToast("Enter name !!!! ");
                            return;
                        } else {
                            // File (or directory) with old name
                            File file = new File(item.getImgUri());
                            Log.e("ah", file.getAbsolutePath());
                            //fireToast(file.getAbsolutePath());
                            // File (or directory) with new name
                            File file2 = new File(file.getParent() + "/" + etNewName.getText().toString() + "." + getFileExt(file.getName()));
                            Log.e("ah", file2.getAbsolutePath());
                            if (file2.exists())
                                try {
                                    throw new java.io.IOException("file exists");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            // Rename file (or directory)
                            boolean success = file.renameTo(file2);
                            if (success) {
                                // File was not successfully renamed
                                fireToast("File renamed ");
                                alertDialog.dismiss();
                            }
                        }
                    }
                });
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                refreshView();
            }
        });
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }


    }

    public String getFileExt(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    boolean toggle = true, toggle2 = true, locker = true;

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (locker) {
            if (toggle) {
                menu.clear();
                getActivity().getMenuInflater().inflate(R.menu.menu2, menu);
            } else {
                menu.clear();
                getActivity().getMenuInflater().inflate(R.menu.menu1, menu);
            }
        } else {
            if (toggle2) {
                menu.clear();
                getActivity().getMenuInflater().inflate(R.menu.menu_lst_selectall, menu);
            } else {
                menu.clear();
                getActivity().getMenuInflater().inflate(R.menu.menu_grid_selectall, menu);
            }
        }


        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search); // sets icon
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView sv = new SearchView(getActivity());

        // modifying the text inside edittext component
        int id = sv.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) sv.findViewById(id);
        textView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        textView.setHint("Search for file ...");
        textView.setHintTextColor(getResources().getColor(android.R.color.holo_red_dark));
        textView.setTextColor(getResources().getColor(android.R.color.holo_red_light));

        // implementing the listener
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchForFile(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        item.setActionView(sv);


    }

    private void searchForFile(String s) {
        listFiles.clear();
        listImages.clear();
        AsyncSearchForImage searchForImage = new AsyncSearchForImage(context);
        searchForImage.execute(s);

    }

    class AsyncSearchForImage extends AsyncTask<String, Void, ArrayList<LstViewImageItem>> {

        Context context = null;
        ProgressDialog dialog = null;

        public AsyncSearchForImage(Context context) {
            this.context = context;
            dialog = new ProgressDialog(this.context);
            dialog.setMessage("Searching for ...");
            dialog.setTitle("Please Wait");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (!dialog.isShowing())
                dialog.show();
        }

        @Override
        protected ArrayList<LstViewImageItem> doInBackground(String... params) {
            ArrayList<LstViewImageItem> list = new ArrayList<LstViewImageItem>();
            String userInput = params[0];
            ////////////////////////
            findFiles();
            for (int i = 0; i < listFiles.size(); i++) {
                File file = listFiles.get(i);
                String imgName = "";
                Date imgCreationDate = null;
                String imgSize = "";
                String imgPath = "";

                imgName = file.getName();//image name associated with extention
//            imgSize = file.getTotalSpace()+"";//image size in kb
                double bytes = file.length();
                double kilobytes = (bytes / 1024);
                NumberFormat formatter = new DecimalFormat("#0.00");
                imgSize = formatter.format(kilobytes);
                imgCreationDate = getCreationDateForFile(file.getAbsolutePath());//image creation date or last modification date
                imgPath = tFileList.get(i);//image uri

                LstViewImageItem lstViewImageItem = new
                        LstViewImageItem(imgPath, imgName, setFormatForDate(imgCreationDate), imgSize);
                listImages.add(lstViewImageItem);
            }
            ///////////////////////////////////////////////////
            //filtering the getten images by image name
            for (int i = 0; i < listImages.size(); i++) {
                if (listImages.get(i).getImgName().toLowerCase().contains(userInput)) {
                    list.add(listImages.get(i));
                }
            }


            return list;

        }

        @Override
        protected void onPostExecute(ArrayList<LstViewImageItem> lstViewImageItems) {
            super.onPostExecute(lstViewImageItems);

            ArrayList<LstViewImageItem> list = lstViewImageItems;

            if (dialog.isShowing())
                dialog.dismiss();

            if (list.isEmpty()) {
                fireToast("No results found");
            }
            listViewAdapterImages = new ListViewAdapterImages(context, list, 0, false);//0 for listview
            listView.setAdapter(listViewAdapterImages);
            listViewAdapterImages.notifyDataSetChanged();


        }
    }

    GridView gv = null;
    ListView lv = null;

    public void refreshView(){

//        if (lv  == null)
//            lv = listView;

        if (lstOrGridVisibileNow == 0) {

            relativeLayout.removeAllViews();
            relativeLayout.removeView(gv);
            lv = createListView();
            lv.setOnItemClickListener(this);
            relativeLayout.addView(lv);

            listFiles.clear();
            listImages.clear();
            listViewAdapterImages = new ListViewAdapterImages(context, listImages, 0, false);//0 for listview
            lv.setAdapter(listViewAdapterImages);
            new AsyncTastGetAllImages(context).execute(SD_CARD_ROOT);
            //listViewAdapterImages.notifyDataSetChanged();

            locker = false;
            toggle2 = true;

        } else {
            relativeLayout.removeAllViews();
            relativeLayout.removeView(lv);
            gv = createGridView();
            gv.setOnItemClickListener(this);
            relativeLayout.addView(gv);

            listFiles.clear();
            listImages.clear();
            listViewAdapterImages = new ListViewAdapterImages(context, listImages, 1, false);//1 for gridview
            gv.setAdapter(listViewAdapterImages);
            new AsyncTastGetAllImages(context).execute(SD_CARD_ROOT);
            listViewAdapterImages.notifyDataSetChanged();

            locker = false;
            toggle2 = false;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.fileFinderExit:
                //System.exit(0);
                getActivity().finish();

                break;
            case R.id.fileFinderHistory:
                break;
            case R.id.fileFinderListView:
                relativeLayout.removeAllViews();
                relativeLayout.removeView(gv);
                lv = createListView();
                lv.setOnItemClickListener(this);
                relativeLayout.addView(lv);

                listFiles.clear();
                listImages.clear();
                listViewAdapterImages = new ListViewAdapterImages(context, listImages, 0, false);//1 for gridview
                lv.setAdapter(listViewAdapterImages);
                new AsyncTastGetAllImages(context).execute(SD_CARD_ROOT);
                lstOrGridVisibileNow = 0;

                locker = true;
                toggle = !toggle;


                break;
            case R.id.fileFinderSetAsHome:
                break;
            case R.id.fileFinderGridView:
                relativeLayout.removeAllViews();
                relativeLayout.removeView(lv);
                gv = createGridView();
                gv.setOnItemClickListener(this);
                relativeLayout.addView(gv);

                listFiles.clear();
                listImages.clear();
                listViewAdapterImages = new ListViewAdapterImages(context, listImages, 1, false);//1 for gridview
                gv.setAdapter(listViewAdapterImages);
                new AsyncTastGetAllImages(context).execute(SD_CARD_ROOT);
                lstOrGridVisibileNow = 1;

                locker = true;
                toggle = !toggle;


                break;
            case R.id.sortBy:
                sortItemsBy();
                break;
            case R.id.direcSortMode:
                break;
            case R.id.fileFinderHome:
                break;
            case R.id.folderSelectAll:

                if (lstOrGridVisibileNow == 0) {

                    relativeLayout.removeAllViews();
                    relativeLayout.removeView(gv);
                    lv = createListView();
                    lv.setOnItemClickListener(this);
                    relativeLayout.addView(lv);

                    listFiles.clear();
                    listImages.clear();
                    listViewAdapterImages = new ListViewAdapterImages(context, listImages, 0, true);//1 for gridview
                    lv.setAdapter(listViewAdapterImages);
                    new AsyncTastGetAllImages(context).execute(SD_CARD_ROOT);
                    listViewAdapterImages.notifyDataSetChanged();

                    locker = false;
                    toggle2 = true;

                } else {
                    relativeLayout.removeAllViews();
                    relativeLayout.removeView(lv);
                    gv = createGridView();
                    gv.setOnItemClickListener(this);
                    relativeLayout.addView(gv);

                    listFiles.clear();
                    listImages.clear();
                    listViewAdapterImages = new ListViewAdapterImages(context, listImages, 1, true);//1 for gridview
                    gv.setAdapter(listViewAdapterImages);
                    new AsyncTastGetAllImages(context).execute(SD_CARD_ROOT);
                    listViewAdapterImages.notifyDataSetChanged();

                    locker = false;
                    toggle2 = false;
                }

                break;
            case R.id.folderDeselectAll:
                if (lstOrGridVisibileNow == 0) {

                    relativeLayout.removeAllViews();
                    relativeLayout.removeView(gv);
                    lv = createListView();
                    lv.setOnItemClickListener(this);
                    relativeLayout.addView(lv);

                    listFiles.clear();
                    listImages.clear();
                    listViewAdapterImages = new ListViewAdapterImages(context, listImages, 0, false);//1 for gridview
                    lv.setAdapter(listViewAdapterImages);
                    new AsyncTastGetAllImages(context).execute(SD_CARD_ROOT);
                    listViewAdapterImages.notifyDataSetChanged();

                    locker = true;
                    toggle = true;

                } else {
                    relativeLayout.removeAllViews();
                    relativeLayout.removeView(lv);
                    gv = createGridView();
                    gv.setOnItemClickListener(this);
                    relativeLayout.addView(gv);

                    listFiles.clear();
                    listImages.clear();
                    listViewAdapterImages = new ListViewAdapterImages(context, listImages, 1, false);//1 for gridview
                    gv.setAdapter(listViewAdapterImages);
                    new AsyncTastGetAllImages(context).execute(SD_CARD_ROOT);
                    listViewAdapterImages.notifyDataSetChanged();

                    locker = toggle = true;//for reinit the menu

                }

                break;
            default:
                break;

        }


        return super.onOptionsItemSelected(item);
    }

    private void sortItemsBy() {

        //getting the view
        View viewSort = LayoutInflater.from(context).inflate(R.layout.layout_sort, null);

        //Accessing the view
        View viewName = viewSort.findViewById(R.id.sort1),
                viewLastMod = viewSort.findViewById(R.id.sort2),
                viewSize = viewSort.findViewById(R.id.sort3), viewType = viewSort.findViewById(R.id.sort4);
        final RadioButton rbSize = (RadioButton) viewSize.findViewById(R.id.rbSize),
                rbType = (RadioButton) viewType.findViewById(R.id.rbType),
                rbLastMod = (RadioButton) viewLastMod.findViewById(R.id.rbLastMod),
                rbName = (RadioButton) viewName.findViewById(R.id.rbName);


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = v.getTag().toString();
                if (tag.equals("sort1")) {
                    rbName.setChecked(true);
                    rbLastMod.setChecked(false);
                    rbSize.setChecked(false);
                    rbType.setChecked(false);
                }
                if (tag.equals("sort2")) {
                    rbName.setChecked(false);
                    rbLastMod.setChecked(true);
                    rbSize.setChecked(false);
                    rbType.setChecked(false);
                }
                if (tag.equals("sort3")) {
                    rbName.setChecked(false);
                    rbLastMod.setChecked(false);
                    rbSize.setChecked(true);
                    rbType.setChecked(false);
                }
                if (tag.equals("sort4")) {
                    rbName.setChecked(false);
                    rbLastMod.setChecked(false);
                    rbSize.setChecked(false);
                    rbType.setChecked(true);
                }
            }
        };

        RadioButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String tag = buttonView.getTag().toString();
                if (tag.equals("sort1") && isChecked) {
                    rbLastMod.setChecked(false);
                    rbSize.setChecked(false);
                    rbType.setChecked(false);
                }
                if (tag.equals("sort2") && isChecked) {
                    rbName.setChecked(false);
                    rbSize.setChecked(false);
                    rbType.setChecked(false);
                }
                if (tag.equals("sort3") && isChecked) {
                    rbName.setChecked(false);
                    rbLastMod.setChecked(false);
                    rbType.setChecked(false);
                }
                if (tag.equals("sort4") && isChecked) {
                    rbName.setChecked(false);
                    rbLastMod.setChecked(false);
                    rbSize.setChecked(false);
                }
            }
        };

        rbName.setChecked(true);
        viewLastMod.setOnClickListener(onClickListener);
        viewName.setOnClickListener(onClickListener);
        viewType.setOnClickListener(onClickListener);
        viewSize.setOnClickListener(onClickListener);

        rbLastMod.setOnCheckedChangeListener(onCheckedChangeListener);
        rbSize.setOnCheckedChangeListener(onCheckedChangeListener);
        rbType.setOnCheckedChangeListener(onCheckedChangeListener);
        rbName.setOnCheckedChangeListener(onCheckedChangeListener);


        viewSort.findViewById(R.id.desc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //organize desc
                //organizeDesc(listImages);
                organizeDesc();
                alertDialog.dismiss();
            }
        });
        viewSort.findViewById(R.id.asc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //organize asc
                organizeAsc();
                alertDialog.dismiss();
            }
        });


        builder = new AlertDialog.Builder(context);
        builder.setView(viewSort);

        alertDialog = builder.create();
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }


    }

    private void organizeAsc() {
        //getting the listImages firstly
        if (lstOrGridVisibileNow == 0) {
            relativeLayout.removeAllViews();
            relativeLayout.removeView(gv);
            lv = createListView();
            lv.setOnItemClickListener(this);
            relativeLayout.addView(lv);

            listFiles.clear();
            listImages.clear();
            AsyncGetAsc asyncGetAsc = new AsyncGetAsc(context);
            asyncGetAsc.execute(SD_CARD_ROOT);


        } else {
            relativeLayout.removeAllViews();
            relativeLayout.removeView(lv);
            gv = createGridView();
            gv.setOnItemClickListener(this);
            relativeLayout.addView(gv);

            listFiles.clear();
            listImages.clear();
            AsyncGetAsc asyncGetAsc = new AsyncGetAsc(context);
            asyncGetAsc.execute(SD_CARD_ROOT);

        }


    }

    private void organizeDesc() {

        //getting the listImages firstly
        if (lstOrGridVisibileNow == 0) {
            relativeLayout.removeAllViews();
            relativeLayout.removeView(gv);
            lv = createListView();
            lv.setOnItemClickListener(this);
            relativeLayout.addView(lv);

            listFiles.clear();
            listImages.clear();
            AsyncGetDesc asyncGetDesc = new AsyncGetDesc(context);
            asyncGetDesc.execute(SD_CARD_ROOT);


        } else {
            relativeLayout.removeAllViews();
            relativeLayout.removeView(lv);
            gv = createGridView();
            gv.setOnItemClickListener(this);
            relativeLayout.addView(gv);

            listFiles.clear();
            listImages.clear();
            AsyncGetDesc asyncGetDesc = new AsyncGetDesc(context);
            asyncGetDesc.execute(SD_CARD_ROOT);

        }


    }

    class AsyncGetDesc extends AsyncTask<String, Void, ArrayList<LstViewImageItem>> {

        Context context = null;
        ProgressDialog dialog = null;
        ArrayList<LstViewImageItem> list = new ArrayList<LstViewImageItem>();

        public AsyncGetDesc(Context context) {
            this.context = context;
            dialog = new ProgressDialog(this.context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setTitle("Please Wait");
            dialog.setCancelable(false);
            dialog.setMessage("Sorting Desc ... ");
            dialog.setIndeterminate(true);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!dialog.isShowing()) {
                dialog.show();
            }

        }

        public ArrayList<LstViewImageItem> getList() {
            return list;
        }

        @Override
        protected ArrayList<LstViewImageItem> doInBackground(String... params) {

            findFiles();
            for (int i = 0; i < listFiles.size(); i++) {
                File file = listFiles.get(i);
                String imgName = "";
                Date imgCreationDate = null;
                String imgSize = "";
                String imgPath = "";

                imgName = file.getName();//image name associated with extention
//            imgSize = file.getTotalSpace()+"";//image size in kb
                double bytes = file.length();
                double kilobytes = (bytes / 1024);
                NumberFormat formatter = new DecimalFormat("#0.00");
                imgSize = formatter.format(kilobytes);
                imgCreationDate = getCreationDateForFile(file.getAbsolutePath());//image creation date or last modification date
                imgPath = tFileList.get(i);//image uri

                LstViewImageItem lstViewImageItem = new
                        LstViewImageItem(imgPath, imgName, setFormatForDate(imgCreationDate), imgSize);
                listImages.add(lstViewImageItem);
            }
            listDesc.clear();
            list = orgDesc(listImages);
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<LstViewImageItem> lstViewImageItems) {
            super.onPostExecute(lstViewImageItems);

            ArrayList<LstViewImageItem> list = lstViewImageItems;

            if (dialog.isShowing())
                dialog.dismiss();

            listViewAdapterImages = new ListViewAdapterImages(context, list, 0, false);//0 for listview
            lv.setAdapter(listViewAdapterImages);
            listViewAdapterImages.notifyDataSetChanged();

        }
    }

    class AsyncGetAsc extends AsyncTask<String, Void, ArrayList<LstViewImageItem>> {

        Context context = null;
        ProgressDialog dialog = null;
        ArrayList<LstViewImageItem> list = new ArrayList<LstViewImageItem>();

        public AsyncGetAsc(Context context) {
            this.context = context;
            dialog = new ProgressDialog(this.context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setTitle("Please Wait");
            dialog.setCancelable(false);
            dialog.setMessage("Sorting Asc ... ");
            dialog.setIndeterminate(true);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!dialog.isShowing()) {
                dialog.show();
            }

        }

        @Override
        protected ArrayList<LstViewImageItem> doInBackground(String... params) {

            findFiles();
            for (int i = 0; i < listFiles.size(); i++) {
                File file = listFiles.get(i);
                String imgName = "";
                Date imgCreationDate = null;
                String imgSize = "";
                String imgPath = "";

                imgName = file.getName();//image name associated with extention
//            imgSize = file.getTotalSpace()+"";//image size in kb
                double bytes = file.length();
                double kilobytes = (bytes / 1024);
                NumberFormat formatter = new DecimalFormat("#0.00");
                imgSize = formatter.format(kilobytes);
                imgCreationDate = getCreationDateForFile(file.getAbsolutePath());//image creation date or last modification date
                imgPath = tFileList.get(i);//image uri

                LstViewImageItem lstViewImageItem = new
                        LstViewImageItem(imgPath, imgName, setFormatForDate(imgCreationDate), imgSize);
                listImages.add(lstViewImageItem);
            }
            listAsc.clear();
            list = orgAsc(listImages);
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<LstViewImageItem> lstViewImageItems) {
            super.onPostExecute(lstViewImageItems);

            ArrayList<LstViewImageItem> list = lstViewImageItems;

            if (dialog.isShowing())
                dialog.dismiss();

            listViewAdapterImages = new ListViewAdapterImages(context, list, 0, false);//0 for listview
            lv.setAdapter(listViewAdapterImages);
            listViewAdapterImages.notifyDataSetChanged();

        }
    }

    ArrayList<LstViewImageItem> listDesc = new ArrayList<LstViewImageItem>();

    public ArrayList<LstViewImageItem> orgDesc(ArrayList<LstViewImageItem> listImages) {
        //list.clear();
        if (listImages != null) {
            if (!listImages.isEmpty()) {
                double max = 0;
                int count = 0;
                for (int i = 0; i < listImages.size(); i++) {
                    double d = Double.parseDouble(listImages.get(i).getImgSizeKb());
                    if (max < d) {
                        max = d;
                        count = i;
                    }
                }
                listDesc.add(listImages.get(count));
                listImages.remove(count);
                //recursion
                orgDesc(listImages);

            }
//            if (listImages.size() == 1)
//                return listImages;
        }
        return listDesc;
    }

    ArrayList<LstViewImageItem> listAsc = new ArrayList<LstViewImageItem>();

    public ArrayList<LstViewImageItem> orgAsc(ArrayList<LstViewImageItem> listImages) {
        //list2.clear();
        if (listImages != null) {
            if (!listImages.isEmpty()) {
                double min = 0;
                int count = 0;
                for (int i = 0; i < listImages.size(); i++) {
                    double d = Double.parseDouble(listImages.get(i).getImgSizeKb());
                    if (min > d) {
                        min = d;
                        count = i;
                    }
                }
                listAsc.add(listImages.get(count));
                listImages.remove(count);
                //recursion
                orgDesc(listImages);

            }
//            if (listImages.size() == 1)
//                return listImages;
        }
        return listAsc;
    }

    private ListView createListView() {
//        ListView listView = new ListView(context);
//        listView.setDividerHeight(2);
//        listView.setDivider(getResources().getDrawable(android.R.color.darker_gray, null));
//
        ListView listView = (ListView) LayoutInflater.from(context).inflate(R.layout.list_view, null);


        return listView;
    }

    private GridView createGridView() {
        GridView gridView = new GridView(context);
        gridView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        gridView.setHorizontalSpacing(1);
        gridView.setVerticalSpacing(1);
        gridView.setNumColumns(3);
        return gridView;
    }


//    private ArrayList<LstViewImageItem> getAllImages(String path, File file) {
//        ArrayList<LstViewImageItem> listImages = new ArrayList<LstViewImageItem>();
//        ArrayList<File> listFiles = new ArrayList<File>();
//
//        File f = new File(path);
//        File[] files = f.listFiles();
//        for (File inFile : files) {
//            if (inFile.isDirectory()) {
//                // is directory
//
//
//            } else {
//                if (isImageFile()){
//                    listFiles.add();
//                }
//            }
//        }
//
//        return listImages;
//    }
}
