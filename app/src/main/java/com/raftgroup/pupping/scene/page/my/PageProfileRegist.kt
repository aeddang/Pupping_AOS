package com.raftgroup.pupping.scene.page.my

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.InputType
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.lib.module.SoftKeyboard
import com.lib.page.PageComponent
import com.lib.page.PageFragment
import com.lib.page.PagePresenter
import com.lib.page.PageView
import com.lib.util.AppUtil
import com.lib.util.PageLog
import com.lib.util.setSize
import com.lib.util.showCustomToast
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.PageProfileRegistBinding
import com.raftgroup.pupping.scene.page.my.component.*
import com.raftgroup.pupping.scene.page.my.model.InputData
import com.raftgroup.pupping.scene.page.my.model.InputDataType
import com.raftgroup.pupping.scene.page.my.model.RadioData
import com.raftgroup.pupping.scene.page.my.model.SelectData
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.*
import com.skeleton.component.dialog.Alert
import com.skeleton.component.graph.Graph
import com.skeleton.component.graph.GraphBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class PageProfileRegist: PageFragment() {
    enum class ProfileType{
        User, Pet
    }
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var ctx: Context
    @Inject lateinit var repository: PageRepository
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: PageProfileRegistBinding
    private lateinit var graphBuilder: GraphBuilder
    override fun onViewBinding(): View {
        binding = PageProfileRegistBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
        repository.disposeLifecycleOwner(this)
    }

    override val hasBackPressAction: Boolean
        get(){
            checkClose()
            return true
        }

    private fun checkClose(){
        Alert.Builder(pagePresenter.activity)
            .setSelectButtons()
            .setText(R.string.profileCancelConfirm)
            .onSelected {
                if (it == 0){
                    pagePresenter.closePopup(pageObject?.key)
                }
            }
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pageTab.setup(null, isBack = true){
            checkClose()
        }
        graphBuilder = GraphBuilder(binding.graphArea, Graph.Type.HolizentalBar)
            .setAnimationType(Graph.AnimationType.EaseInSine)
            .setColor(R.color.brand_primary)
            .setRange(1.0)
        initSetup()
        setupInput()
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val ctx = context
        ctx ?: return
        binding.btnPrev.setOnClickListener { onPrev() }
        binding.btnNext.setOnClickListener { onNext() }
        binding.btnSkip.setOnClickListener { next() }
    }
    var type:ProfileType = ProfileType.Pet
    private var currentComponent:PageComponent? = null
    private var currentName:String = ""
    private var selectedProfileImage:Bitmap? = null
    private var step:Int  = 0
    private var profile:PetProfile? = null
    private var userProfile:UserProfile? = null
    private var steps: List<InputData> = arrayListOf()

    private var inputData:InputData? = null
    private var input:String = ""
    private var selectedIdx:Int = -1
    private var selectedDate:LocalDate? = null
    private var selectedImage:Bitmap? = null

    override val pageChileren:ArrayList<PageView>?
        get(){
            currentComponent ?: return arrayListOf()
            return arrayListOf(currentComponent!!)
        }

    @Suppress("UNREACHABLE_CODE")
    override fun onPageParams(params: Map<String, Any?>): PageView {
        (params[PageParam.type] as? ProfileType)?.let { type = it }
        return super.onPageParams(params)
    }
    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
    }

    private fun initSetup(){
        when(type){
            ProfileType.User -> {
                userProfile = UserProfile()
                steps = arrayListOf(
                    InputData(
                        InputDataType.Text,
                        ctx.getString( R.string.profileRegistNickName ),
                        ctx.getString( R.string.profileRegistNameTip ),
                        placeHolder = ctx.getString( R.string.profileNickNamePlaceHolder)
                    )
                )
            }
            ProfileType.Pet -> {
                if (dataProvider.user.currentPet != null){
                    profile = dataProvider.user.currentPet
                } else {
                    profile = PetProfile().init(true)
                }
                steps = arrayListOf(
                    InputData(
                        InputDataType.Text,
                        ctx.getString( R.string.profileRegistName ),
                        ctx.getString( R.string.profileRegistNameTip ),
                        placeHolder = ctx.getString( R.string.profileNamePlaceHolder)
                    ),
                    InputData(
                        InputDataType.Image,
                        ctx.getString( R.string.profileRegistImage )
                    ),
                    InputData(
                        InputDataType.Text,
                        ctx.getString( R.string.profileRegistSpecies ),
                        ctx.getString( R.string.profileRegistNameTip ),
                        placeHolder = ctx.getString( R.string.profileSpeciesPlaceHolder )
                    ),
                    InputData(
                        InputDataType.Date,
                        ctx.getString( R.string.profileRegistBirth)
                    ),
                    InputData(
                        InputDataType.Select,
                        ctx.getString( R.string.profileRegistGender ),
                        tabs=arrayListOf<SelectData>(
                            SelectData(0, R.drawable.ic_male, R.string.male ,R.color.brand_fourthExtra),
                            SelectData(0, R.drawable.ic_female, R.string.female ,R.color.brand_fiveth)
                        )
                    ),
                    InputData(
                        InputDataType.Text,
                        ctx.getString( R.string.profileRegistMicroFin ),
                        info=ctx.getString( R.string.profileRegistMicroFinInfo ),
                        placeHolder = ctx.getString( R.string.profileMicroFinPlaceHolder ),
                        keyboardType = InputType.TYPE_CLASS_NUMBER,
                        isOption = true
                    ),
                    InputData(
                        InputDataType.Radio,
                        ctx.getString( R.string.profileRegistHealth ),
                        checks = arrayListOf(
                            RadioData(false, R.string.profileRegistNeutralized),
                            RadioData(false, R.string.profileRegistDistemperVaccinated),
                            RadioData(false, R.string.profileRegistHepatitisVaccinated),
                            RadioData(false, R.string.profileRegistParovirusVaccinated),
                            RadioData(false, R.string.profileRegistRabiesVaccinated)
                        )
                    )
                )
            }
        }
        binding.textTotal.text = "of ${steps.size}"
        if (steps.size > 1) {
            context?.let { ctx ->
                val barW = ctx.resources.getDimension(R.dimen.bar_medium)
                val barH = ctx.resources.getDimension(R.dimen.line_mediumExtra)
                val size = Size((barW * steps.size).toInt(), barH.toInt())
                binding.graphArea.setSize(size)
                graphBuilder.setSize(size)
                graphBuilder.setRange(steps.size.toDouble())
            }
        }
    }

    private fun setupInput(){
        if (step >= steps.size) return
        val cdata = steps[step]
        if (step == 0) {
            currentName = ""
        }
        inputData = cdata
        binding.textStep.text = "step ${(step+1)}"
        graphBuilder.show((step+1).toDouble())
        if(cdata.type != InputDataType.Image && selectedProfileImage != null){
            binding.imgProfileBox.visibility = View.VISIBLE
        } else{
            binding.imgProfileBox.visibility = View.GONE
        }
        when (step) {
            0 -> {
                binding.btnPrev.visibility = View.GONE
            }
            steps.size-1 -> {
                binding.btnNext.text = context?.getString(R.string.btnComplete)
                binding.btnPrev.visibility = View.VISIBLE
            }
            else -> {
                binding.btnNext.text = context?.getString(R.string.btnNext)
                binding.btnPrev.visibility = View.VISIBLE
            }
        }
        binding.btnSkip.visibility = if(cdata.isOption) View.VISIBLE else View.GONE
        binding.body.removeAllViews()
        val ctx = context ?: return
        currentComponent = when(cdata.type) {
            InputDataType.Text->{
                input = cdata.inputValue
                val inputCell = InputCell(ctx)
                inputCell.setup(cdata, currentName){ c, isNext ->
                    input = c
                    binding.btnNext.selected = isComplete
                    if (isNext) onNext()
                }
                inputCell
            }
            InputDataType.Select->{
                selectedIdx = cdata.selectedIdx
                val selectTab = SelectTab(ctx)
                selectTab.setup(cdata, currentName){
                    selectedIdx = it
                    binding.btnNext.selected = isComplete
                }
                selectTab
            }
            InputDataType.Radio->{
                val selectRadio = SelectRadio(ctx)
                selectRadio.setup(cdata, currentName){
                    binding.btnNext.selected = isComplete
                }
                selectRadio
            }
            InputDataType.Date->{
                selectedDate = cdata.selectedDate
                val selectDatePicker = SelectDatePicker(ctx)
                selectDatePicker.setup(cdata, currentName){
                    selectedDate = it
                    binding.btnNext.selected = isComplete
                }
                selectDatePicker
            }
            InputDataType.Image->{
                selectedImage = cdata.selectedImage
                val selectImagePicker = SelectImagePicker(ctx)
                selectImagePicker.setup(cdata, currentName){
                    selectedImage = it
                    binding.btnNext.selected = isComplete
                }
                selectImagePicker
            }
        }
        currentComponent?.let {
            binding.body.addView(it)
        }
        binding.btnNext.selected = isComplete
    }

    private val isComplete:Boolean
    get(){
        return when (inputData?.type){
            InputDataType.Text -> input.isNotEmpty()
            InputDataType.Image -> selectedImage != null
            InputDataType.Select -> selectedIdx != -1
            InputDataType.Date -> selectedDate != null
            InputDataType.Radio -> true
            else -> true
        }
    }

    private fun update() : Boolean {
        if (step >= steps.size) { return false }
        when (step) {
            0-> {
                if (input.isEmpty()) { return false }
                currentName = "$input${context?.getString(R.string.owner)} "
                profile?.update(ModifyPetProfileData(nickName = input))
                userProfile?.update(ModifyUserProfileData(nickName = input))
            }
            1-> {
                val image = selectedImage ?: return false
                selectedProfileImage = image
                binding.imgProfile.setImageBitmap(image)
                profile?.update(image.copy(image.config, image.isMutable))
            }
            2-> {
                if (input.isEmpty()) { return false }
                profile?.update(ModifyPetProfileData(species=input))
            }
            3-> {
                val date = selectedDate ?: return false
                profile?.update(ModifyPetProfileData(birth=date))
            }
            4-> {
                val gender:Gender = if( selectedIdx == 0 )  Gender.Male else Gender.Female
                profile?.update(ModifyPetProfileData(gender=gender))
            }
            5-> {
                profile?.update(ModifyPetProfileData(microfin=input))
            }
            6-> {
                val inputData = inputData ?: return false
                profile?.update(ModifyPetProfileData(
                        neutralization=inputData.checks?.get(0)?.isCheck,
                distemper=inputData.checks?.get(1)?.isCheck,
                hepatitis=inputData.checks?.get(2)?.isCheck,
                parovirus=inputData.checks?.get(3)?.isCheck,
                rabies=inputData.checks?.get(4)?.isCheck))
            }
            else -> {}
        }
        return true
    }

    private fun saveInput(){
        inputData?.let {
            when (it.type) {
                InputDataType.Text ->  it.inputValue = input
                InputDataType.Image -> it.selectedImage = selectedImage
                InputDataType.Select -> it.selectedIdx = selectedIdx
                InputDataType.Date -> it.selectedDate = selectedDate
                InputDataType.Radio -> {}
            }
        }
        inputData = null
        selectedIdx = -1
        selectedImage = null
        input = ""
        selectedDate = LocalDate.now()
    }
    private fun onPrev(){
        update()
        prev()
    }

    private fun prev(){
        saveInput()
        val willStep = step - 1
        if (willStep < 0) { return }
        step = willStep
        setupInput()
    }
    private fun onNext(){
        if(update()){
            next()
        } else {
            Toast(context).showCustomToast(R.string.alertNeedInput, pagePresenter.activity)
        }
    }

    private fun next(){
        SoftKeyboard( context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager? ).hideKeyBoard()
        saveInput()
        val willStep = step + 1
        if (willStep >= steps.size ){
            setupCompleted()
        } else {
            step = willStep
            setupInput()
        }
    }

    private fun setupCompleted(){
        val user = dataProvider.user.snsUser ?: return
        when (type) {
            ProfileType.User ->{
                dataProvider.requestData(
                    ApiQ(appTag, ApiType.UpdateUser, requestData = ModifyUserProfileData(nickName = userProfile?.nickName?.value))
                )
            }
            ProfileType.Pet ->{
                val profile = this.profile ?: return
                dataProvider.requestData(
                    ApiQ(appTag, ApiType.RegistPet, requestData = profile)
                )
            }

        }
        pagePresenter.closePopup(pageObject?.key)


    }
}