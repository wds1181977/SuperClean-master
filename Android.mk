LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := library-2.4.0 \
                               SocialSDK_WeiXin_1 \
                               umeng-analytics-v5.6.7 \
                               umeng-update-v2.5.0 \
							   android-support-v4 \
							   android-support-v7-recyclerview 
		
							   
						   
				
                               
LOCAL_SRC_FILES := $(call all-java-files-under,src)


LOCAL_PACKAGE_NAME := SuperClean

LOCAL_CERTIFICATE := platform


include $(BUILD_PACKAGE)



include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES :=library-2.4.0:libs/library-2.4.0.jar \
 SocialSDK_WeiXin_1:libs/SocialSDK_WeiXin_1.jar \
 umeng-analytics-v5.6.7:libs/umeng-analytics-v5.6.7.jar \
 umeng-update-v2.5.0:libs/umeng-update-v2.5.0.jar 

 
 
include $(BUILD_MULTI_PREBUILT)
