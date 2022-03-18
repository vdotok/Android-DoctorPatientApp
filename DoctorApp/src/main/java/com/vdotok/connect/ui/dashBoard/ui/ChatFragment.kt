package com.vdotok.connect.ui.dashBoard.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.navigation.Navigation
import com.vdotok.connect.R
import com.vdotok.connect.databinding.LayoutChatFragmentBinding
import com.vdotok.connect.dialogs.PatientHealthDialog
import com.vdotok.connect.extensions.*
import com.vdotok.connect.models.GroupModel
import com.vdotok.connect.prefs.Prefs
import com.vdotok.connect.ui.dashBoard.adapter.ChatListAdapter
import com.vdotok.connect.ui.dashBoard.adapter.OnMediaItemClickCallbackListner
import com.vdotok.connect.ui.fragments.ChatMangerListenerFragment
import com.vdotok.connect.utils.*
import com.vdotok.connect.utils.ApplicationConstants.REQUEST_CODE_GALLERY
import com.vdotok.connect.utils.ApplicationConstants.SENSOR_TIME_OUT
import com.vdotok.connect.utils.ImageUtils.calculateInSampleSize
import com.vdotok.connect.utils.ImageUtils.copyFileToInternalStorage
import com.vdotok.connect.utils.ImageUtils.encodeToBase64
import com.vdotok.connectSdk.manager.ChatManager
import com.vdotok.connectSdk.models.*
import com.vdotok.connectSdk.models.Message
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask


/**
 * Created By: VdoTok
 * Date & Time: On 5/3/21 At 1:26 PM in 2021
 */
class ChatFragment: ChatMangerListenerFragment(), OnMediaItemClickCallbackListner{

    private var selectedItemPosition: Int = 0
    private lateinit var binding: LayoutChatFragmentBinding
    lateinit var adapter: ChatListAdapter
    private lateinit var prefs: Prefs
    var groupModel : GroupModel? = null

    var file: File? = null
    var VideoPath: String? = null
    var fileType = 0
    val directoryName: String = "Vdotok-chat"
    var model: Message? = null
    val prefix = "#"
    val sufix = "#"
    var isHeart: Boolean = false
    var offBodyValue: String = ""

    private var cManger: ChatManager? = null
    var listOfChunks: ArrayList<FileModel> = ArrayList()
    var fileChunkMaps: MutableMap<String, ArrayList<FileModel>> = mutableMapOf()
    private val usersList = ArrayList<String>()


    private var timer: CountDownTimer? = null
    var title : ObservableField<String> = ObservableField<String>()
    var mMessageText : ObservableField<String> = ObservableField<String>()
    private var typingText : ObservableField<String> = ObservableField<String>()

    var showTypingText : ObservableBoolean = ObservableBoolean(false)

    private var loginUserRefId = ""


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = LayoutChatFragmentBinding.inflate(inflater, container, false)
        prefs = Prefs(context)

        init()
        if (listOfChunks.isNotEmpty())
            listOfChunks.clear()


        return binding.root
    }

    private fun initUserListAdapter() {
        activity?.applicationContext?.let {
            adapter = ChatListAdapter(it, this, loginUserRefId, getSaveChat(), this, {}){
                sendAcknowledgeMsgToGroup(it)
            }
            binding.rcvMsgList.adapter = adapter
        }
    }

    private fun getSaveChat(): List<Message> {
        return (activity as DashboardActivity).mapGroupMessages[groupModel?.channelName]
            ?: return ArrayList()
    }

    private fun init() {
        activity?.let {
            cManger = ChatManager.getInstance(it)
            prefs = Prefs(activity)
        }

        (activity as DashboardActivity).mListener = this

        groupModel = arguments?.get(GroupModel.TAG) as GroupModel?

        groupModel?.let {
            cManger?.subscribeTopic(it.channelKey, it.channelName)
        }

        binding.chatToolbar.imgBack.setOnClickListener {
          activity?.hideKeyboard()
          openInboxFragment()
        }


        binding.chatToolbar.typingUserName = typingText


        setModelData()

        initUserListAdapter()
        setListeners()

    }

     fun getUserName(model: Message): String {
        val participants = groupModel?.participants?.find { it.refID == model.from}
         return participants?.fullname.toString()
    }


    private fun setModelData() {
        binding.chatToolbar.groupTitle = title
        binding.chatInputLayout.messageText = mMessageText
        mMessageText.set("")
        binding.chatInputLayout.imgMic.performSingleClick {
            selectAudio()
        }
        binding.chatToolbar.showTypingText = showTypingText

        groupModel?.let {
            if (it.participants.size <= 2){
                it.participants.forEach { userName->
                    if (!userName.fullname.equals(prefs.loginInfo?.fullName)) {
                        title.set(userName.fullname)
                    }
                }
            } else{
                title.set(it.groupTitle)
            }
        }
        loginUserRefId = prefs.loginInfo?.refId.toString()
    }

    private fun setListeners(){

        binding.chatInputLayout.edtWriteMessage.afterTextChanged {
            if (getMessageCheck(mMessageText)) {
                handleAfterTextChange(mMessageText.get().toString())
            }
        }

        binding.chatInputLayout.imgSend.setOnClickListener {
            if (getMessageCheck(mMessageText)) {
                sendTextMessage()
            }
        }

//        binding.chatInputLayout.imgMore.performSingleClick {
//            activity?.supportFragmentManager?.let { AttachmentGroupDialog(
//                this::selectVideo,
//                this::selectAudio,
//                this::selectDoc,
//                this::takePhotoFromCamera,
//                this::openMap,
//                this::openContact
//            ).show(it, AttachmentGroupDialog.TAG) }
//        }

        binding.chatInputLayout.imgGallery.performSingleClick {
                selectImage()
        }


        binding.chatInputLayout.imgMore.performSingleClick {
            fetchSensorDataFromPatient()
        }



//        binding.chatInputLayout.imgHeart.setOnClickListener {
//            (activity as DashboardActivity).sendPhoto()
//            binding.chatInputLayout.tvSensorText.visibility = View.VISIBLE
//        }

        (activity as DashboardActivity).let {
//            val dataAdapter = ArrayAdapter(it, android.R.layout.simple_spinner_item, WearManager.getInstance(it).availableSensorsList)
//            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

//            binding.chatInputLayout.spinner.adapter = dataAdapter

            binding.chatInputLayout.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                ) {
                    selectedItemPosition = position
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }


        }



    }

    private fun fetchSensorDataFromPatient() {
        if(binding.chatInputLayout.tvSensorText.isVisible.not()) {
            activity?.supportFragmentManager?.let {
                PatientHealthDialog { sensorType ->
                    fetchSensor(sensorType)
                }.show(it, PatientHealthDialog.TAG)
            }
        }

    }

    private fun getMessageCheck(msg: ObservableField<String>): Boolean {
        if (msg.get().toString().isEmpty()) {
            binding.chatInputLayout.imgSend.isEnabled = false
            binding.chatInputLayout.imgSend.setColorFilter(
                    ContextCompat.getColor(
                            requireContext(),
                            R.color.grayish_3
                    )
            )
            return false

        } else {
            binding.chatInputLayout.imgSend.isEnabled = true
            binding.chatInputLayout.imgSend.setColorFilter(
                    ContextCompat.getColor(
                            requireContext(),
                            R.color.bold_green
                    )
            )
           return true
        }
    }


    companion object {
        const val TYPING_START = "1"
        const val TYPING_STOP = "0"
        const val TAG_FRAGMENT_CHAT = "CHAT_FRAGMENT"
        const val REQUEST_CODE_VIDEO = 101
        const val REQUEST_CODE_DOC = 102
        const val REQUEST_CODE_AUDIO = 103
        const val CAMERA = 109
        @JvmStatic
        fun newInstance() = ChatFragment().apply {}
    }


    /**
     * Function to handle on text changes when user is writing a message
     * @param text String to observe text change
     * */
    private fun handleAfterTextChange(text: String) {

        //setSendButtonState(text)

        if(text.isNotEmpty()){
            sendTypingMessage(loginUserRefId, true)

            timer?.cancel()
            timer = object : CountDownTimer(1500, 1000) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    sendTypingMessage(loginUserRefId, false)
                }
            }.start()
        }
    }


    /**
     * Function to scroll recyclerview to last index
     * */
    private fun scrollToLast() {
        binding.rcvMsgList.smoothScrollToPosition(adapter.itemCount)
    }


    /**
     * Function to send message upon clicking send button
     * */
    private fun sendTextMessage(data: String? = null, SensorData: MessageType? = null) {
        var text = mMessageText.get().toString()
        var messageType: MessageType = MessageType.text
        SensorData?.let { messageType = SensorData}
        data?.let { text = it }

        groupModel?.let {
            if(text.isNotEmpty()){
                val chatModel = Message(
                        System.currentTimeMillis().toString(),
                        it.channelName,
                        it.channelKey,
                        loginUserRefId,
                        messageType,
                        text.trim(),
                        0f,
                        getIsGroupMessage(),
                        ReceiptType.SENT.value
                )
                cManger?.publishMessage(chatModel)
                mMessageText.set("")
            }
        }
    }

    private fun getIsGroupMessage(): Boolean {
        return groupModel?.participants?.size!! > 1
    }


    private fun fetchSensor(sensor: SensorType){
        sendTextMessage(sensor.value, MessageType.fetchingSensorData)

        startSensorTimer()
    }


    private var sensorHandler: Handler? = null
    private val sensorRunnable = Runnable {
            binding.chatInputLayout.tvSensorText.hide()
            showTimeOutText()
    }

    private fun startSensorTimer() {
        sensorHandler = Handler(Looper.getMainLooper())
        sensorHandler?.postDelayed(sensorRunnable, SENSOR_TIME_OUT)
    }

    private fun stopSensorTimerTask() {
        if (sensorHandler != null) {
            sensorHandler?.removeCallbacksAndMessages(null)
            sensorHandler = null
        }
    }

    private fun showTimeOutText() {
        binding.chatInputLayout.tvTimeout.show()
        object: CountDownTimer(1500, 1500) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                binding.chatInputLayout.tvTimeout.hide()
            }
        }.start()
    }

    var tempURI: Uri? = null
    private fun saveFileToStorage(
            bytes: ByteArray,
            displayName: String,
            mimeType: String,
            path: String,
            contentUri: Uri
    ) {

        val resol = activity?.applicationContext?.contentResolver
        val contentValu = ContentValues()
        contentValu.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        contentValu.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        contentValu.put(MediaStore.MediaColumns.RELATIVE_PATH, path)

        val imageurl = resol?.insert(contentUri, contentValu)

        val parcelFileDescriptor =
            activity?.applicationContext?.contentResolver?.openFileDescriptor(imageurl!!, "w", null)

        val fileOutputStream = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)
        fileOutputStream.write(bytes)
        fileOutputStream.close()
        imageurl?.let { uri ->
            activity?.applicationContext?.let { context->
                file = File(copyFileToInternalStorage(context, uri, directoryName))
                contentValu.clear()
                activity?.applicationContext?.contentResolver?.update(uri, contentValu, null, null)

                tempURI = uri
            }
        }
    }



    private fun openMap(){
        binding.root.showSnackBar(R.string.in_progress)

    }

    private fun openContact(){
        binding.root.showSnackBar(R.string.in_progress)

    }

    private fun selectImage() {
        val pickPhoto = Intent(
                Intent.ACTION_GET_CONTENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        pickPhoto.type = "image/*"
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pickPhoto.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        startActivityForResult(pickPhoto, ApplicationConstants.REQUEST_CODE_GALLERY)
    }

    private fun selectAudio() {
        val pickAudio = Intent(
                Intent.ACTION_GET_CONTENT,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        )

        pickAudio.type = "audio/*"
        pickAudio.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pickAudio.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        startActivityForResult(pickAudio, REQUEST_CODE_AUDIO)
    }

    private fun selectVideo() {
        val pickVideo = Intent(
                Intent.ACTION_GET_CONTENT,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )

        pickVideo.type = "video/*"
        pickVideo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pickVideo.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        startActivityForResult(pickVideo, REQUEST_CODE_VIDEO)
    }

    private fun selectDoc() {
        val pickDoc = Intent(Intent.ACTION_GET_CONTENT)
        pickDoc.type = "*/*"
        val mimeTypes = arrayOf(
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .doc & .docx
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // .ppt & .pptx
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xls & .xlsx
                "text/plain",
                "application/pdf",
                "application/zip",
                "application/vnd.android.package-archive"
        )
        pickDoc.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        pickDoc.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(pickDoc, REQUEST_CODE_DOC)
    }



    var selectedFile: File? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if(resultCode == Activity.RESULT_OK){

            when (requestCode) {
                REQUEST_CODE_GALLERY -> {
                    handleSelectionFromGallery(data)
                }
                REQUEST_CODE_VIDEO -> {
                    handleSelectionFromVideos(data)
                }
                REQUEST_CODE_AUDIO -> {
                    handleSelectionFromAudio(data)
                }
                CAMERA -> {
                    handleSelectionFromCamera(data)
                }
                REQUEST_CODE_DOC -> {
                    handleSelectionFromDocuments(data)
                }
            }

            Handler().post {
                groupModel?.let {
                    cManger?.sendFileToGroup(it.channelKey, it.channelName, selectedFile, fileType)
                }
            }
        }
            
    }

    private fun handleSelectionFromDocuments(data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            data?.data?.let { data ->
                activity?.let { context ->
                    val filePath = copyFileToInternalStorage(context, data, "document")
                    val FileBytes = converFileToByteArray(filePath)
                    saveFileToStorage(
                            FileBytes!!,
                            "${System.currentTimeMillis()}",
                            "application/pdf",
                            "${Environment.DIRECTORY_DOCUMENTS}/$directoryName",
                            MediaStore.Files.getContentUri("external")
                    )
                }
            }
        } else {
            file = getFileData(activity as Context, data?.data, MediaType.FILE)
        }
        selectedFile  = file
        fileType = 3

    }

    private fun handleSelectionFromCamera(data: Intent?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.applicationContext?.let { context->
                val byteArray = ImageUtils.convertBitmapToByteArray(
                        context,
                        data?.extras?.get("data") as Bitmap
                )

                saveFileToStorage(
                        byteArray!!,
                        "${System.currentTimeMillis()}",
                        "image/jpeg",
                        "${Environment.DIRECTORY_PICTURES}/$directoryName",
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            }
        } else {
            file = getFileData(
                    activity as Context, getImageUri(
                    activity?.applicationContext!!, data?.extras?.get(
                    "data"
            ) as Bitmap
            ), MediaType.IMAGE
            )

        }

        selectedFile = file
        fileType = 0

    }

    private fun handleSelectionFromAudio(data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.applicationContext?.let { context ->
                data?.data?.let { data ->
                    val AudioPath = copyFileToInternalStorage(context, data, "audio")
                    val AudioBytes = converFileToByteArray(AudioPath)
                    saveFileToStorage(
                            AudioBytes!!,
                            "${System.currentTimeMillis()}",
                            "audio/x-wav",
                            "${Environment.DIRECTORY_MUSIC}/$directoryName",
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    )
                }
            }
        } else {
            file = getFileData(activity as Context, data?.data, MediaType.AUDIO)

        }
        selectedFile = file
        fileType = 1
    }

    private fun handleSelectionFromVideos(data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.applicationContext?.let { context ->
                data?.data?.let { data ->
                    VideoPath = ImageUtils.copyFileToInternalStorage(context, data, "video")
                    val VideoBytes = converFileToByteArray(VideoPath)
                    saveFileToStorage(
                            VideoBytes!!,
                            "${System.currentTimeMillis()}",
                            "video/mp4",
                            "${Environment.DIRECTORY_MOVIES}/$directoryName",
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    )
                }

            }
        } else {
            file = getFileData(activity as Context, data?.data, MediaType.VIDEO)
        }
        selectedFile = file
        fileType = 2

    }

    private fun handleSelectionFromGallery(data: Intent?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.applicationContext?.let { context->
                val byteArray = ImageUtils.convertImageToByte(
                        context,
                        Uri.parse(data?.data.toString())
                )

                saveFileToStorage(
                        byteArray!!,
                        "${System.currentTimeMillis()}",
                        "image/jpeg",
                        "${Environment.DIRECTORY_PICTURES}/$directoryName",
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            }
        } else {

            file = getFileData(activity as Context, data?.data, MediaType.IMAGE)

        }

        selectedFile = file
        fileType = 0

    }


    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
                inContext.contentResolver,
                inImage,
                "Title",
                null
        )
        return Uri.parse(path)
    }
    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }


    /**
     * Function to send typing message to other user that the "other user is typing"
     * @param userName User name of the user who is typing
     * @param isTyping Boolean object to inform a user has started typing
     * */
    private fun sendTypingMessage(userName: String, isTyping: Boolean) {

        groupModel?.let {
            val content = if(isTyping) TYPING_START else TYPING_STOP
            val chatModel = Message(
                    System.currentTimeMillis().toString(),
                    it.channelName,
                    it.channelKey,
                    userName,
                    MessageType.typing,
                    content,
                    0f,
                    getIsGroupMessage()
            )

            cManger?.sendTypingMessage(chatModel, chatModel.key, chatModel.to)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        stopSensorTimerTask()
    }

    override fun onConnectionSuccess() {
        groupModel?.let {
            cManger?.subscribeTopic(it.channelKey, it.channelName)
        }
    }


    override fun onNewMessage(message: Message) {
        activity?.runOnUiThread {
            if (message.type == MessageType.fetchingSensorData){
                binding.chatInputLayout.tvSensorText.show()
            }
            else {
                binding.chatInputLayout.tvSensorText.hide()
                if (message.key == groupModel?.channelKey) {
                     usersList.clear()
                     adapter.addItem(message)
                     sendAcknowledgeMsgToGroup(message)
                     scrollToLast()
                 }
            }
            if(message.type == MessageType.sensorDataFetched){
                stopSensorTimerTask()
            }
        }
    }

    override fun onTypingMessage(message: Message) {
        if (message.key == groupModel?.channelKey && (message.from == loginUserRefId).not()) {
            showOnTypingMessage(message)
        }
        Log.d(TAG_FRAGMENT_CHAT, message.toString())
    }

    override fun onReceiptReceived(model: ReadReceiptModel) {
        activity?.runOnUiThread {
            if((model.from != loginUserRefId))
                adapter.updateMessageForReceipt(model)
        }
    }

    override fun onBytesArrayReceived(payload: ByteArray?) {
        activity?.runOnUiThread {
            payload?.let {
                val model = Message(
                        System.currentTimeMillis().toString(),
                        groupModel?.channelName ?: "",
                        groupModel?.channelKey ?: "",
                        loginUserRefId,
                        MessageType.media,
                        encodeToBase64(it),
                        0f,
                        getIsGroupMessage()
                )
                adapter.addItem(model)
            }
        }

    }

    override fun onFileReceivedCompleted(
            headerModel: HeaderModel,
            byteArray: ByteArray,
            msgId: String
    ) {
        checkAndroidVersionToSave(headerModel, byteArray)
        groupModel?.let {

            when (headerModel.type) {

                MediaType.IMAGE.value -> {
                    model = makeImageItemModel(file, headerModel, it, msgId)
                }
                MediaType.VIDEO.value -> {
                    model = makeVideoItemModel(file, headerModel, it, msgId)
                }
                MediaType.AUDIO.value -> {
                    model = makeAudioItemModel(file, headerModel, it, msgId)
                }
                MediaType.FILE.value -> {
                    model = makeFileItemModel(file, headerModel, it, msgId)
                }
            }
        }



        model?.let { (activity as DashboardActivity).sendAcknowledgmentOnMediaMessage(it) }
        binding.progressBar.hide()
    }

    override fun onFileSendingFailed(headerId: String) {
        super.onFileSendingFailed(headerId)
        activity?.runOnUiThread {
            deleteTempFile()
        }
    }

    override fun onFileSendingComplete() {
        super.onFileSendingComplete()
        activity?.runOnUiThread {
            deleteTempFile()
        }
    }

    private fun deleteTempFile() {
        val resolver = activity?.applicationContext?.contentResolver
        tempURI?.let {
            val result: Int? = resolver?.delete(it, null, null)
            if (result != null && result > 0) {
                Log.d("Tag", "File deleted")
            }
        }
    }

    private fun checkAndroidVersionToSave(headerModel: HeaderModel, byteArray: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when(headerModel.type) {
                MediaType.IMAGE.value -> saveFileToStorage(
                        byteArray,
                        "${System.currentTimeMillis()}",
                        "image/jpeg",
                        "${Environment.DIRECTORY_PICTURES}/$directoryName",
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                MediaType.VIDEO.value -> saveFileToStorage(
                        byteArray,
                        "${System.currentTimeMillis()}",
                        "video/mp4",
                        "${Environment.DIRECTORY_MOVIES}/$directoryName",
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                MediaType.AUDIO.value -> saveFileToStorage(
                        byteArray,
                        "${System.currentTimeMillis()}",
                        "audio/x-wav",
                        "${Environment.DIRECTORY_MUSIC}/$directoryName",
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                )
                MediaType.FILE.value -> saveFileToStorage(
                        byteArray,
                        "${System.currentTimeMillis()}",
                        "application/pdf",
                        "${Environment.DIRECTORY_DOCUMENTS}/$directoryName",
                        MediaStore.Files.getContentUri(
                                "external"
                        )
                )

            }

        } else {
            val fileName = "file_".plus(System.currentTimeMillis()).plus(".").plus(headerModel.fileExtension)
            val filePath = createAppDirectory(headerModel.type) + "/$fileName"
            file = saveFileDataOnExternalData(filePath, byteArray)

        }

    }

    private fun  makeAudioItemModel(
            file: File?,
            headerModel: HeaderModel,
            groupModel: GroupModel,
            msgId: String
    ): Message? {
        return file?.toUri()?.let {
            Message(
                    msgId,
                    groupModel.channelName,
                    groupModel.channelKey,
                    headerModel.from,
                    MessageType.media,
                    it.toString(),
                    0f,
                    getIsGroupMessage(),
                    ReceiptType.SENT.value,
                    headerModel.type
            )
        }
    }

    private fun makeImageItemModel(
            file: File?,
            headerModel: HeaderModel,
            groupModel: GroupModel,
            msgId: String
    ): Message? {
        val bitmap = file?.let {
            getBitmap(it, 500, 500) }

        return bitmap?.let {
            encodeToBase64(it)?.let { base64String ->
                Message(
                        msgId,
                        groupModel.channelName,
                        groupModel.channelKey,
                        headerModel.from,
                        MessageType.media,
                        base64String,
                        0f,
                        getIsGroupMessage(),
                        ReceiptType.SENT.value,
                        headerModel.type
                )
            }
        }
    }


    /**
     * Function to get Bitmap with params
     * @param file File object to get the bitmap from
     * @param reqWidth required width of the resource
     * @param reqHeight required height of the resource
     * @return Return Bitmap type object
     * */
    private fun getBitmap(file: File, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(file.path, options)
    }

    private fun makeVideoItemModel(
            file: File?,
            headerModel: HeaderModel,
            groupModel: GroupModel,
            msgId: String
    ): Message? {
        return Message(
                msgId,
                groupModel.channelName,
                groupModel.channelKey,
                headerModel.from,
                MessageType.media,
                file?.toUri().toString(),
                0f,
                getIsGroupMessage(),
                ReceiptType.SENT.value,
                headerModel.type
        )
    }

    private fun makeFileItemModel(
            file: File?,
            headerModel: HeaderModel,
            groupModel: GroupModel,
            msgId: String
    ): Message? {
        return Message(
                msgId,
                groupModel.channelName,
                groupModel.channelKey,
                headerModel.from,
                MessageType.media,
                file?.toUri().toString(),
                0f,
                getIsGroupMessage(),
                ReceiptType.SENT.value,
                headerModel.type
        )
    }

    override fun onChunkReceived(fileModel: FileModel) {
        if (fileChunkMaps.containsKey(fileModel.headerId)) {
            var modelList = fileChunkMaps[fileModel.headerId]
            if (modelList?.size!! > 0) {
                modelList.remove(fileModel)
                fileChunkMaps[fileModel.headerId] = modelList
                if (modelList.isNotEmpty()) {
                    (activity as DashboardActivity).publishCustomPacketMessage(
                            modelList.first(),
                            groupModel?.channelKey ?: "",
                            groupModel?.channelName ?: ""
                    )
                }
            } else {
                if (fileChunkMaps.containsKey(fileModel.headerId)) {
                    fileChunkMaps.remove(fileModel.headerId)
                }
            }
        }
    }

    private fun openInboxFragment() {
        Navigation.findNavController(binding.root).navigate(R.id.action_open_inbox_fragment)
    }

    /** function to handle sending acknowledgment message to the group that the message is received and seen
     * @param myMessage MqttMessage object containing details sent for the acknowledgment in group
     * */
    private fun sendAcknowledgeMsgToGroup(myMessage: Message) {
        if(myMessage.from != loginUserRefId) {
            val receipt = ReadReceiptModel(
                    ReceiptType.SEEN.value,
                    myMessage.key,
                    System.currentTimeMillis(),
                    myMessage.id,
                    loginUserRefId,
                    myMessage.to
            )

            cManger?.publishPacketMessage(receipt, receipt.key, receipt.to)
        }
    }

    /**
     * Function to inflate typing message received view
     * @param message is the Typing Message Object that are receiving fom server
     * */
    private fun showOnTypingMessage(message: Message) {
        if(message.content == TYPING_START) {
            //binding.chatToolbar.status.show()
                message.from = getUserName(message)
                showTypingText.set(true)
                typingText.set(getNameOfUsers(message))
                hideTypingText()
            }

    }

    private fun openFolder() {
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            intent.setDataAndType(
                    Uri.parse(
                            Environment.getExternalStorageDirectory().toString()
                                    + File.separator + "cPass" + File.separator
                    ), "*/*"
            )

            activity?.startActivity(Intent.createChooser(intent, "Complete action using"))
        } else {

            intent.setDataAndType(
                    Uri.parse(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()
                                    + File.separator + "cPass" + File.separator
                    ), "*/*"
            )
            activity?.startActivity(Intent.createChooser(intent, "Open folder"))
        }

    }

    /**
     * Function to hide typing message received view
     * */
    private fun hideTypingText() {
        Timer().schedule(timerTask {
            activity?.runOnUiThread {
                //binding.chatToolbar.status.invisible()
                showTypingText.set(false)
            }
        }, 2000)
    }

    /**
     * Helper Function to get users of a typing message received
     * @param message is the Message Object of Users that are receiving fom server
     * */
    private fun getNameOfUsers(message: Message): String {
        if (usersList.contains(message.from).not()) {
            usersList.add(message.from)
        }

        return when (usersList.size) {
            0 -> ""
            1 -> "${usersList[0]} is typing..."
            2 -> "${usersList[usersList.size - 1]} and ${usersList[usersList.size - 2]} are typing..."
            else -> {
                val size = usersList.size
                "${usersList[size - 1]},${usersList[size - 2]} and ${size.minus(2)} others are typing..."
            }
        }
    }

    override fun onFileClick() {
        openFolder()
    }


    override fun onSensorDataReceived(data: String) {
        activity?.runOnUiThread {
            stopSensorTimerTask()
            sendTextMessage(data)
            binding.chatInputLayout.tvSensorText.visibility = View.GONE
        }
    }

}
