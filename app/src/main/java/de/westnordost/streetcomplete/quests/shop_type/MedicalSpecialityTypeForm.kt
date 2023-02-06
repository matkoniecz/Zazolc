package de.westnordost.streetcomplete.quests.shop_type

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ViewShopTypeBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog

class MedicalSpecialityTypeForm : AbstractOsmQuestForm<ShopTypeAnswer>() {

    override val contentLayoutResId = R.layout.view_shop_type // TODO?
    private val binding by contentViewBinding(ViewShopTypeBinding::bind)

    private lateinit var radioButtons: List<RadioButton>
    private var selectedRadioButtonId: Int = 0
    private lateinit var featureCtrl: FeatureViewController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioButtons = listOf(binding.vacantRadioButton, binding.replaceRadioButton, binding.leaveNoteRadioButton)
        for (radioButton in radioButtons) {
            radioButton.setOnClickListener { selectRadioButton(it) }
        }

        featureCtrl = FeatureViewController(featureDictionary, binding.featureView.textView, binding.featureView.iconView)
        featureCtrl.countryOrSubdivisionCode = countryOrSubdivisionCode

        binding.featureView.root.background = null
        binding.featureContainer.setOnClickListener {
            selectRadioButton(binding.replaceRadioButton)

            SearchFeaturesDialog(
                requireContext(),
                featureDictionary,
                element.geometryType,
                countryOrSubdivisionCode,
                featureCtrl.feature?.name,
                ::filterOnlySpecialitiesOfMedicalDoctors,
                ::onSelectedFeature,
                listOf( // based on https://taginfo.openstreetmap.org/keys/healthcare%3Aspeciality#values
                    // with alternative medicine skipped
                    "amenity/doctors/general",
                    // chiropractic - skipped (alternative medicine)
                    "amenity/doctors/ophthalmology",
                    "amenity/doctors/paediatrics",
                    "amenity/doctors/gynaecology",
                    //biology skipped as that is value for laboratory
                    "amenity/dentist",
                    // psychiatry - https://github.com/openstreetmap/id-tagging-schema/issues/778
                    "amenity/doctors/orthopaedics",
                    "amenity/doctors/internal",
                    "healthcare/dentist/orthodontics",
                    "amenity/doctors/dermatology",
                    // osteopathy - skipped (alternative medicine)
                    "amenity/doctors/otolaryngology",
                    "amenity/doctors/radiology",
                    // vaccination? that is tagged differently, right? TODO
                    "amenity/doctors/cardiology",
                    "amenity/doctors/surgery", // TODO? really for doctors? Maybe that is used primarily for hospitals?
                    // physiotherapy
                    // urology
                    // emergency
                    // dialysis
                )
            ).show()
        }
    }

    private fun filterOnlySpecialitiesOfMedicalDoctors(feature: Feature): Boolean {
        if(feature.tags["amenity"] in listOf("dentist", "veterinary")) {
            return true
        }
        if(!feature.tags.containsKey("healthcare:speciality")) {
            return false
        }
        return feature.tags["amenity"] in "doctors"
    }

    private fun onSelectedFeature(feature: Feature) {
        featureCtrl.feature = feature
        checkIsFormComplete()
    }

    override fun onClickOk() {
        when (selectedRadioButtonId) {
            R.id.vacantRadioButton    -> applyAnswer(IsShopVacant)
            R.id.leaveNoteRadioButton -> composeNote()
            R.id.replaceRadioButton   -> applyAnswer(ShopType(featureCtrl.feature!!.addTags))
        }
    }

    override fun isFormComplete() = when (selectedRadioButtonId) {
        R.id.vacantRadioButton,
        R.id.leaveNoteRadioButton -> true
        R.id.replaceRadioButton   -> featureCtrl.feature != null
        else                      -> false
    }

    private fun selectRadioButton(radioButton: View) {
        selectedRadioButtonId = radioButton.id
        for (b in radioButtons) {
            b.isChecked = selectedRadioButtonId == b.id
        }
        checkIsFormComplete()
    }
}
