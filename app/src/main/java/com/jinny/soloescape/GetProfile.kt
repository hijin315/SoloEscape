package com.jinny.soloescape

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.storage.FirebaseStorage
import com.jinny.soloescape.databinding.GetProfileBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.HashMap

class GetProfile : AppCompatActivity() {
    companion object {  // static 프로퍼티
        const val PICK_PROFILE_FROM_ALBUM = 1 // 갤러리에서 사진 가져오기
    }

    var storage: FirebaseStorage? = null
    var auth: FirebaseAuth? = null

    var photoUri: Uri? = null //사용자가 갤러리에서 선택한 사진의 파일 경로로
    lateinit var binding: GetProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GetProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //storage 초기화
        storage = FirebaseStorage.getInstance()

        //auth 초기화
        auth = FirebaseAuth.getInstance()


        //갤러리 실행
        goGallery()

        binding.btnProfileUpload.setOnClickListener {
            if (binding.nameEditText.text.isNotEmpty()) {
                contentUpload()
            } else {
                Toast.makeText(this, "이름을 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnProfileExit.setOnClickListener {
            Toast.makeText(this, "모두 입력해주셔야 합니다", Toast.LENGTH_SHORT).show()
        }


    }

    private fun goGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK) //갤러리로 이동하겠다는 뜻!
        galleryIntent.type = "image/*" //선택한 파일의 종류를 지정해준다 (이미지만 지정하겠다는 뜻)
        intent.putExtra("crop", true)
        startActivityForResult(galleryIntent, PICK_PROFILE_FROM_ALBUM)
    }

    private fun cropImage(uri: Uri?) {
        CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_PROFILE_FROM_ALBUM -> {
                data?.data?.let { uri ->
                    cropImage(uri)
                }
                photoUri = data?.data
                binding.ivProfileImage.setImageURI(photoUri)
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result.uri?.let {
                        binding.ivProfileImage.setImageBitmap(result.bitmap)
                        binding.ivProfileImage.setImageURI(result.uri)
                        photoUri = result.uri

                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error = result.error
                    Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                //finish()
            }

        }
    }

    private fun contentUpload() {
        val uid = auth?.currentUser?.uid
        //서버 스토리지에 접근하기!
        val storageRef = uid?.let { storage?.reference?.child("profile")?.child(it) }

        // 서버 스토리지에 파일 업로드하기!
        storageRef?.putFile(photoUri!!)?.continueWithTask() {
            return@continueWithTask storageRef.downloadUrl
            //나중에 이미지를 다른 곳에서 불러오고 할떄 url을 가져올수있게해놓음
        }?.addOnSuccessListener {
            upload(it)
            Toast.makeText(this, "업로드 성공", Toast.LENGTH_SHORT).show()
        }?.addOnCanceledListener {
            //업로드 취소 시
        }?.addOnFailureListener {
            //업로드 실패 시
        }
    }

    private fun upload(imageUri: Uri) {
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        val name = binding.nameEditText.text.toString()
        val storageRef = FirebaseStorage.getInstance().reference
            .child("profile")
            .child(uid!!)
        var uri = ""
        storageRef.putFile(imageUri!!).continueWithTask {
            return@continueWithTask storageRef.downloadUrl
        }.addOnSuccessListener {
            uri = it.toString()
            var intent = Intent();
            intent.putExtra("imageUrl", uri)
            intent.putExtra("name", name)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }.addOnFailureListener {
            finish()
        }
        //액티비티 종료


    }
}