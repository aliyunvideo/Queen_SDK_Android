package com.alivc.live.queenbeauty.injector;

import com.alivc.live.queenbeauty.models.AliLiveBeautyFaceShapeType;
import com.alivc.live.queenbeauty.models.AliLiveBeautyParams;
import com.alivc.live.beautyui.bean.BeautyItemBean;
import com.alivc.live.beautyui.bean.BeautyTabBean;
import com.alivc.live.queenbeauty.R;

import java.util.ArrayList;

import static com.alivc.live.queenbeauty.constant.QueenConstant.TAB_INDEX_BEAUTY;
import static com.alivc.live.queenbeauty.constant.QueenConstant.TAB_NAME_BEAUTY;

public class QueenBeautyDataInjector {
    /// *** 美颜 *** ///
    private static final String ITEM_NAME_FACE_SHAPE = "脸型";
    private static final String ITEM_NAME_BP_SKIN_WHITENING = "美白";
    private static final String ITEM_NAME_BP_SKIN_BUFFING = "磨皮";
    private static final String ITEM_NAME_BP_SKIN_SHARPEN = "锐化";
    private static final String ITEM_NAME_FACE_SHAPE_BIG_EYE = "大眼";
    private static final String ITEM_NAME_FACE_SHAPE_PHILTRUM = "人中";
    private static final String ITEM_NAME_FACE_SHAPE_THIN_MANDIBLE = "下颌骨";
    private static final String ITEM_NAME_FACE_SHAPE_THIN_NOSE = "瘦鼻";
    private static final String ITEM_NAME_FACE_SHAPE_MOUTH_WIDTH = "唇宽";
    private static final String ITEM_NAME_FACE_SHAPE_MOUTH_HIGH = "唇高";
    private static final String ITEM_NAME_FACE_SHAPE_CUT_CHEEK = "瘦颧骨";
    private static final String ITEM_NAME_FACE_SHAPE_NOSEWING = "缩鼻翼";
    private static final String ITEM_NAME_FACE_SHAPE_CUT_FACE = "削脸";
    private static final String ITEM_NAME_FACE_SHAPE_THIN_JAW = "下巴";
    private static final String ITEM_NAME_FACE_SHAPE_EYE_ANGLE2 = "眼睑下至";
    private static final String ITEM_NAME_FACE_SHAPE_LOWER_JAW = "下巴拉长";
    private static final String ITEM_NAME_FACE_SHAPE_CANTHUS = "眼距";
    private static final String ITEM_NAME_FACE_SHAPE_NASAL_HEIGHT = "鼻长";
    private static final String ITEM_NAME_FACE_SHAPE_THIN_FACE = "瘦脸";
    private static final String ITEM_NAME_FACE_SHAPE_LONG_FACE = "脸长";


    public static BeautyTabBean getBeautyTabBean() {
        return new BeautyTabBean(TAB_INDEX_BEAUTY, TAB_NAME_BEAUTY, true, true, getBeautyFaceShapeItemBeans());
    }

    private static ArrayList<BeautyItemBean> getBeautyFaceShapeItemBeans() {
        ArrayList<BeautyItemBean> itemBeans = new ArrayList<BeautyItemBean>();

        BeautyItemBean faceShape = new BeautyItemBean(BeautyItemBean.BeautyType.BEAUTY_NONE, ITEM_NAME_FACE_SHAPE, R.drawable.icon_beauty_face_shape);

        BeautyItemBean divider = new BeautyItemBean();

        BeautyItemBean bpSkinBuffing = new BeautyItemBean(AliLiveBeautyParams.kAliLiveParamsBPSkinBuffing, BeautyItemBean.BeautyType.BEAUTY_PARAM,
                ITEM_NAME_BP_SKIN_BUFFING, R.drawable.icon_beauty_bp_skin_buffing_disable, R.drawable.icon_beauty_bp_skin_buffing_enable,
                0, 1, 0.6f);
        BeautyItemBean bpSkinSharpen = new BeautyItemBean(AliLiveBeautyParams.kAliLiveParamsBPSkinSharpen, BeautyItemBean.BeautyType.BEAUTY_PARAM,
                ITEM_NAME_BP_SKIN_SHARPEN, R.drawable.icon_beauty_bp_skin_sharpen_disable, R.drawable.icon_beauty_bp_skin_sharpen_enable,
                0, 1, 0.8f);
        BeautyItemBean bpSkinWhitening = new BeautyItemBean(AliLiveBeautyParams.kAliLiveParamsBPSkinWhitening, BeautyItemBean.BeautyType.BEAUTY_PARAM,
                ITEM_NAME_BP_SKIN_WHITENING, R.drawable.icon_beauty_bp_skin_whitening_disable, R.drawable.icon_beauty_bp_skin_whitening_enable,
                0, 1, 0.6f);

        BeautyItemBean faceShapeBigEye = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeBigEye, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_BIG_EYE, R.drawable.icon_beauty_face_shape_big_eye_disable, R.drawable.icon_beauty_face_shape_big_eye_enable,
                0, 1, 0);
        BeautyItemBean faceShapePhiltrum = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapePhiltrum, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_PHILTRUM, R.drawable.icon_beauty_face_shape_philtrum_disable, R.drawable.icon_beauty_face_shape_philtrum_enable,
                -1, 1, 0);
        BeautyItemBean faceShapeThinMandible = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeThinMandible, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_THIN_MANDIBLE, R.drawable.icon_beauty_face_shape_thin_mandible_disable, R.drawable.icon_beauty_face_shape_thin_mandible_enable,
                0, 1, 0);
        BeautyItemBean faceShapeThinNose = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeThinNose, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_THIN_NOSE, R.drawable.icon_beauty_face_shape_thin_nose_disable, R.drawable.icon_beauty_face_shape_thin_nose_enable,
                0, 1, 0);
        BeautyItemBean faceShapeMouthWidth = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeMouthWidth, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_MOUTH_WIDTH, R.drawable.icon_beauty_face_shape_mouth_width_disable, R.drawable.icon_beauty_face_shape_mouth_width_enable,
                -1, 1, 0);
        BeautyItemBean faceShapeMouthHigh = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeMouthHigh, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_MOUTH_HIGH, R.drawable.icon_beauty_face_shape_mouth_high_disable, R.drawable.icon_beauty_face_shape_mouth_high_enable,
                -1, 1, 0);
        BeautyItemBean faceShapeCutCheek = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeCutCheek, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_CUT_CHEEK, R.drawable.icon_beauty_face_shape_cut_cheek_disable, R.drawable.icon_beauty_face_shape_cut_cheek_enable,
                0, 1, 0);
        BeautyItemBean faceShapeNosewing = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeNosewing, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_NOSEWING, R.drawable.icon_beauty_face_shape_nosewing_disable, R.drawable.icon_beauty_face_shape_nosewing_enable,
                0, 1, 0);
        BeautyItemBean faceShapeCutFace = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeCutFace, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_CUT_FACE, R.drawable.icon_beauty_face_shape_cut_face_disable, R.drawable.icon_beauty_face_shape_cut_face_enable,
                0, 1, 0);
        BeautyItemBean faceShapeThinJaw = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeThinJaw, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_THIN_JAW, R.drawable.icon_beauty_face_shape_thin_jaw_disable, R.drawable.icon_beauty_face_shape_thin_jaw_enable,
                0, 1, 0);
        BeautyItemBean faceShapeEyeAngle2 = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeEyeAngle2, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_EYE_ANGLE2, R.drawable.icon_beauty_face_shape_eye_angle2_disable, R.drawable.icon_beauty_face_shape_eye_angle2_enable,
                -1, 1, 0);
        BeautyItemBean faceShapeLowerJaw = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeLowerJaw, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_LOWER_JAW, R.drawable.icon_beauty_face_shape_lower_jaw_disable, R.drawable.icon_beauty_face_shape_lower_jaw_enable,
                -1, 1, 0);
        BeautyItemBean faceShapeCanthus = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeCanthus, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_CANTHUS, R.drawable.icon_beauty_face_shape_canthus_disable, R.drawable.icon_beauty_face_shape_canthus_enable,
                -1, 1, 0);
        BeautyItemBean faceShapeNasalHeight = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeNasalHeight, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_NASAL_HEIGHT, R.drawable.icon_beauty_face_shape_nasal_height_disable, R.drawable.icon_beauty_face_shape_nasal_height_enable,
                -1, 1, 0);
        BeautyItemBean faceShapeThinFace = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeThinFace, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_THIN_FACE, R.drawable.icon_beauty_face_shape_thin_face_disable, R.drawable.icon_beauty_face_shape_thin_face_enable,
                0, 1, 0);
        BeautyItemBean faceShapeLongFace = new BeautyItemBean(AliLiveBeautyFaceShapeType.kAliLiveFaceShapeLongFace, BeautyItemBean.BeautyType.BEAUTY_FACE_SHAPE,
                ITEM_NAME_FACE_SHAPE_LONG_FACE, R.drawable.icon_beauty_face_shape_long_face_disable, R.drawable.icon_beauty_face_shape_long_face_enable,
                0, 1, 0);

        itemBeans.add(faceShape);
        itemBeans.add(divider);
        itemBeans.add(bpSkinBuffing);
        itemBeans.add(bpSkinSharpen);
        itemBeans.add(bpSkinWhitening);
        itemBeans.add(faceShapeBigEye);
        itemBeans.add(faceShapePhiltrum);
        itemBeans.add(faceShapeThinMandible);
        itemBeans.add(faceShapeThinNose);
        itemBeans.add(faceShapeMouthWidth);
        itemBeans.add(faceShapeMouthHigh);
        itemBeans.add(faceShapeCutCheek);
        itemBeans.add(faceShapeNosewing);
        itemBeans.add(faceShapeCutFace);
        itemBeans.add(faceShapeThinJaw);
        itemBeans.add(faceShapeEyeAngle2);
        itemBeans.add(faceShapeLowerJaw);
        itemBeans.add(faceShapeCanthus);
        itemBeans.add(faceShapeNasalHeight);
        itemBeans.add(faceShapeThinFace);
        itemBeans.add(faceShapeLongFace);

        return itemBeans;
    }
}
