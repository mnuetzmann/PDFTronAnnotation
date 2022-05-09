package com.labconco.annotationsample

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Color.GREEN
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.navigation.fragment.findNavController
import com.labconco.annotationsample.databinding.FragmentFirstBinding
import com.pdftron.pdf.Annot
import com.pdftron.pdf.ColorPt
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.annots.FreeText
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.AppUtils
import com.pdftron.pdf.utils.Utils

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(),
    PDFViewCtrl.PageChangeListener,
    ToolManager.AnnotationModificationListener
{

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            requireContext().assets.open("forms/test_form.pdf").let { stream ->
                PDFDoc(stream).let { doc ->
                    pdfViewCtrl.doc = doc

                    pdfViewCtrl.apply {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            setVerticalAlign(-1)
                        } else {
                            setHorizontalAlign(-1)
                        }

                        addPageChangeListener(this@FirstFragment)
                        AppUtils.setupPDFViewCtrl(this)

                        ToolManager(this).let {
                            toolManager = it
                            it.addAnnotationModificationListener(this@FirstFragment)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun changeAnnotBorderColor(annot: Annot, @ColorInt color: Int) {
        with(binding) {
            // Locks the document first as accessing annotation/doc
            // information isn't thread safe.
            pdfViewCtrl.docLock(true) {
                val colorPt: ColorPt = Utils.color2ColorPt(color)
                // if color is transparent, then color component number is 0.
                val colorCompNum: Int = if (color == Color.TRANSPARENT) 0 else 3
                // if the annotation is a FreeText annotation
                if (annot.type == Annot.e_FreeText) {
                    val freeText = FreeText(annot)
                    freeText.setLineColor(colorPt, colorCompNum)
                } else {
                    annot.setColor(colorPt, colorCompNum)
                }

                annot.refreshAppearance()
                pdfViewCtrl.update()
            }
        }
    }

    override fun onPageChange(p0: Int, p1: Int, p2: PDFViewCtrl.PageChangeState?) {
        with(binding) {
            pdfViewCtrl.apply {
                val page = pdfViewCtrl.doc.getPage(currentPage)
                val count = page.numAnnots

                if (count == 0) {
                    // No annotations on the page.
                } else {
                    for (i in 0 until count) {
                        val annot = page.getAnnot(i)
                        if (annot.isValid) {
                            // Just hard-code the border of ALL annotations to GREEN
                            changeAnnotBorderColor(annot, GREEN)
                        }
                    }
                }
            }
        }
    }

    override fun onAnnotationsAdded(annots: MutableMap<Annot, Int>?) {
    }

    override fun onAnnotationsPreModify(annots: MutableMap<Annot, Int>?) {
    }

    override fun onAnnotationsModified(annots: MutableMap<Annot, Int>?, extra: Bundle?) {

    }

    override fun onAnnotationsPreRemove(annots: MutableMap<Annot, Int>?) {
    }

    override fun onAnnotationsRemoved(annots: MutableMap<Annot, Int>?) {
    }

    override fun onAnnotationsRemovedOnPage(pageNum: Int) {
    }

    override fun annotationsCouldNotBeAdded(errorMessage: String?) {
    }
}