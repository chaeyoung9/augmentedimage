package com.google.ar.sceneform.samples.augmentedimage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.samples.common.helpers.SnackbarHelper;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AugmentedImageActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ModelRenderable pikachu, eevee, pokeball;
    private ImageView fitToScanView;
    private Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();;
    private Map<AugmentedImage, AugmentedImageNode> imtemp = new HashMap<>();
    private Collection<AugmentedImage> updatedAugmentedImages;
    private AnchorNode aNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AnchorNode tempNode;
                aNode.getAnchor().detach();

                fitToScanView.setVisibility(View.VISIBLE);
            }
        });

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        //model 1
        ModelRenderable.builder()
                .setSource(this, R.raw.pikachu)
                .build()
                .thenAccept(renderable -> pikachu = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        //model 2
        ModelRenderable.builder()
                .setSource(this, R.raw.eevee)
                .build()
                .thenAccept(renderable -> eevee = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        //model 3
        ModelRenderable.builder()
                .setSource(this, R.raw.pokeball)
                .build()
                .thenAccept(renderable -> pokeball = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (pikachu == null || eevee == null || pokeball == null) {
                        return;
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
    }

    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        if (frame == null) {
            return;
        }

        updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {

                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = "Detected Image " + augmentedImage.getIndex();
                    SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);
                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        Anchor anchor = augmentedImage.createAnchor(augmentedImage.getCenterPose());
                        aNode = new AnchorNode(anchor);
                        aNode.setParent(arFragment.getArSceneView().getScene());

                        AugmentedImageNode augNode = new AugmentedImageNode(this);
                        augmentedImageMap.put(augmentedImage, augNode);

                        TransformableNode tNode = new TransformableNode(arFragment.getTransformationSystem());
                        tNode.getScaleController().setMinScale(0.01f);
                        tNode.getScaleController().setMaxScale(0.05f);
                        tNode.getTranslationController().setEnabled(false);
                        tNode.setParent(aNode);

                        switch (augmentedImage.getIndex()) {
                            case 0:
                                tNode.setRenderable(pikachu);
                                break;

                            case 1:
                                tNode.setRenderable(eevee);
                                break;

                            case 2:
                                tNode.setRenderable(pokeball);
                                break;
                        }

                        tNode.select();
                    }

                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }
}