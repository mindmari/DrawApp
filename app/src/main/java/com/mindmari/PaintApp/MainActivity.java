package com.mindmari.PaintApp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.mindmari.PaintApp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity
{
    private static final int PERMISSION_CODE = 1000;
    private static final int GALLERY_REQUEST = 1002;
    ActivityMainBinding binding;
    //Диалог выбора толщины кисти
    AlertDialog brushSizeDialog;
    //Диалог выбора формы
    AlertDialog shapeDialog;
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpListeners();

        //Настроить диалоги
        setUpDialogs();
    }
    //----------------------------------------------------------------------------------------------
    //Настроить диалог выбора толщины кисти
    private void setUpDialogs()
    {
        //Вьюшка для инфлейта диалогом
        View v = getLayoutInflater().inflate(R.layout.dialog_brush_size, null);
        //Поиск Slider в этой вьюшке
        Slider slider = v.findViewById(R.id.seekBarBrush);
        //Инициализация диалога выбрра толщины
        brushSizeDialog = new AlertDialog.Builder(this)
                .setTitle("Выберите толщину кисти")
                .setView(v)
                .setPositiveButton("Применить", (dialog, which) -> binding.drawingView.setStrokeWidth(slider.getValue()))
                .setNegativeButton("Отмена", null)
                .create();

        //Инициализация диалога выбрра формы
        shapeDialog = new AlertDialog.Builder(this)
                .setTitle("Выберите форму")
                .setSingleChoiceItems(getResources().getStringArray(R.array.shapes), 5, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (which)
                        {
                            case 0:
                                binding.drawingView.setDrawShape(DrawingView.TRIANGLE);
                                break;
                            case 1:
                                binding.drawingView.setDrawShape(DrawingView.CIRCLE);
                                break;
                            case 2:
                                binding.drawingView.setDrawShape(DrawingView.LINE);
                                break;
                            case 3:
                                binding.drawingView.setDrawShape(DrawingView.SQUARE);
                                break;
                            case 4:
                                binding.drawingView.setDrawShape(DrawingView.RECTANGLE);
                                break;
                        }
                        dialog.dismiss();
                    }
                }).create();
    }
    //----------------------------------------------------------------------------------------------
    private void setUpListeners()
    {
        //Очистить холст
        binding.clear.setOnClickListener(v -> binding.drawingView.clearAll());
        //Сохранить картинку
        binding.save.setOnClickListener(v -> saveImage());
        //загрузить из галереи
        binding.upload.setOnClickListener(v -> uploadFromGallery());
        //Рисовать кистью
        binding.brush.setOnClickListener(v -> binding.drawingView.setDrawShape(DrawingView.BRUSH));
        //Выбор формы рисования
        binding.shape.setOnClickListener(v -> shapeDialog.show());
        //Толщина кисти
        binding.brushSize.setOnClickListener(v -> {
            //new BrushSizeDialog().show(getSupportFragmentManager(), "size");
            brushSizeDialog.show();
        });
        //Выбор цвета
        binding.color.setOnClickListener(v -> new ColorPickerDialog.Builder(this,
                "Выберите цвет",
                "Применить",
                "Отмена",
                (i, s) -> binding.drawingView.setDrawPaintColor(i, s),
                binding.drawingView.getCurrentColor(),
                ColorShape.CIRCLE).show());
    }
    //----------------------------------------------------------------------------------------------
    //Сохранить картинку
    private void saveImage()
    {
        //Имя файла - текущее время в мс
        String fileName = System.currentTimeMillis() + ".jpg";
        //Путь по которму будет файл
        String savePath = getExternalFilesDir("Saved").getAbsolutePath();
        //Неполный путь сохранённого файла для отображения пользователям
        String pathToShow = "/Android/data/by.gregorovich.mpaint/files/Saved";
        File file = new File(savePath, fileName);
        try(FileOutputStream outputStream = new FileOutputStream(file))
        {
            Bitmap bitmap = binding.drawingView.getCanvasBitmap();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Snackbar.make(binding.getRoot(), "Saved to \"" + pathToShow + "\"", BaseTransientBottomBar.LENGTH_SHORT)
                    .show();
        } catch (IOException e)
        {
            addToLog("Exception: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------------------------
    //Загрузить из галереи
    private void uploadFromGallery()
    {
        //Запустить галерею
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }
    //----------------------------------------------------------------------------------------------
    //Проверить какая тема установлена в данный момент
    private boolean isNightTheme()
    {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDark = false;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) isDark = true;
        return isDark;
    }
    //----------------------------------------------------------------------------------------------
    //Запрос разрешений
    private void requestPermissions()
    {
        //Проверить разрешения камеры и записи в хранилище
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            //Инициализация массива строк с необходимыми разрешениями
            String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            //Запросить разрешение
            requestPermissions(permissions, PERMISSION_CODE);
        }
    }
    //----------------------------------------------------------------------------------------------
    //Обработка полученного ответа для разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == PERMISSION_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)
            {
                addToLog("Разрешения получены");
            } else
            {
                Snackbar.make(binding
                        .getRoot(), "Разрешеня не получены", BaseTransientBottomBar.LENGTH_SHORT)
                        .show();
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            if (requestCode == GALLERY_REQUEST)
            {
                if (data.getData() != null)
                {
                    Uri selectedImage = data.getData();
                    //Вызвать окно редактирования картинки
                    CropImage.activity(selectedImage)
                            .setAspectRatio(binding.drawingView.getWidthView(), binding.drawingView.getHeightView())
                            .setFixAspectRatio(true)
                            .start(this);
                } else
                {
                    //Показать сообщение об ошибке
                    Snackbar.make(binding.getRoot(), "Ошибка загрузки", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                try
                {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri resultUri = result.getUri();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    binding.drawingView.setBitmap(scaleBitmap(bitmap));
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    //Изменить размер картинки
    private Bitmap scaleBitmap(Bitmap bitmap)
    {
        int srcW = bitmap.getWidth();
        int srcH = bitmap.getHeight();
        int viewH = binding.drawingView.getHeightView();
        int viewW = binding.drawingView.getWidthView();
        int dstW = srcW;
        int dstH = srcH;
        addToLog("old W - " + srcW + "| H - " + srcH + "|");

        //Пока ширина или высота картинки больше чем DrawingView
        if (dstW > viewW || dstH > viewH)
        {
            if (srcH == srcW) //Квадратная картинка
            {
                addToLog("Квадратная картинка");
                dstW = (srcH*viewH)/srcH;
                dstH = dstW;
                addToLog("new W - " + dstW + "| H - " + dstW + "|");
                bitmap = Bitmap.createScaledBitmap(bitmap, dstW, dstW, true);

            } else //Вертикальная или горизонтальная картинка
            {
                addToLog("Вертикальная картинка");

                //Исправить высоту
                if (dstH > viewH)
                {
                    dstH = viewH;
                    dstW = (srcW*dstH)/srcH;
                    addToLog("Высоту W - " + dstW + "| H - " + dstH + "|");
                    bitmap = Bitmap.createScaledBitmap(bitmap, dstW, dstH, true);
                }
                //Исправить ширину
                if (dstW > viewW)
                {
                    dstW = viewW;
                    dstH = (dstW*srcH)/srcW;
                    addToLog("Ширину W - " + dstW + "| H - " + dstH + "|");
                    bitmap = Bitmap.createScaledBitmap(bitmap, dstW, dstH, true);
                }
            }
        }

        //Если ширина или высота картинки меньше чем DrawingView
        if (dstW < viewW || dstH < viewH)
        {
            if (srcH == srcW) //Квадратная картинка
            {
                addToLog("Квадратная картинка");
                dstW = (srcH*viewH)/srcH;
                addToLog("new W - " + dstW + "| H - " + dstW + "|");
                bitmap = Bitmap.createScaledBitmap(bitmap, dstW, dstW, true);

            } else //Вертикальная или горизонтальная картинка
            {
                addToLog("Вертикальная картинка");

                //Исправить высоту
                if (dstH < viewH)
                {
                    dstH = viewH;
                    dstW = (srcW*dstH)/srcH;
                    addToLog("Высоту W - " + dstW + "| H - " + dstH + "|");
                    bitmap = Bitmap.createScaledBitmap(bitmap, dstW, dstH, true);
                }
                //Исправить ширину
                if (dstW < viewW)
                {
                    dstW = viewW;
                    dstH = (dstW*srcH)/srcW;
                    addToLog("Ширину W - " + dstW + "| H - " + dstH + "|");
                    bitmap = Bitmap.createScaledBitmap(bitmap, dstW, dstH, true);
                }
            }
        }

        return bitmap;
    }
    //----------------------------------------------------------------------------------------------
    private void addToLog(String msg)
    {
        Log.d("TAG", msg);
    }
    //----------------------------------------------------------------------------------------------
}