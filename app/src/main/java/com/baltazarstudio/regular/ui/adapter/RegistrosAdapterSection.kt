package com.baltazarstudio.regular.ui.adapter

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.baltazarstudio.regular.R
import com.baltazarstudio.regular.context.RegistroContext
import com.baltazarstudio.regular.model.Movimento
import com.baltazarstudio.regular.observer.Trigger
import com.baltazarstudio.regular.observer.Events
import com.baltazarstudio.regular.ui.registros.DetalhesRegistroDialog
import com.baltazarstudio.regular.util.Utils
import com.baltazarstudio.regular.util.Utils.Companion.formattedDate
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.layout_section_header_registro.view.*
import kotlinx.android.synthetic.main.layout_section_item_registro.view.*

class RegistrosAdapterSection(
    private val adapter: SectionedRecyclerViewAdapter,
    private val ano: Int,
    private val mes: Int,
    private val movimentos: List<Movimento>
) : Section(
    SectionParameters.builder().headerResourceId(R.layout.layout_section_header_registro)
        .itemResourceId(R.layout.layout_section_item_registro).build()
) {
    
    
    private var mOnCheckableModeItemSelectedListener: (Int) -> Unit = {}
    var checkableMode: Boolean = false
    private var expanded: Boolean = true
    private var multiSelectModeEnabled = true
    
    
    fun setOnCheckableModeItemSelectedListener(listener: (Int) -> Unit) {
        mOnCheckableModeItemSelectedListener = listener
    }
    
    fun disableMultiSelectMode() { multiSelectModeEnabled = false }
    
    override fun getContentItemsTotal(): Int {
        return if (expanded) movimentos.size else 0
    }
    
    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
        return HeaderViewHolder(view)
    }
    
    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        (holder as HeaderViewHolder).bindView()
    }
    
    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ItemViewHolder(view)
    }
    
    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as ItemViewHolder).bindView(movimentos[position])
    }
    
    private inner class HeaderViewHolder(headerView: View) : RecyclerView.ViewHolder(headerView) {
        fun bindView() {
            var total = 0.0
            for (movimento in movimentos) total += movimento.valor
            itemView.tv_section_header_registros_title.text = Utils.getMesString(mes, ano)
            itemView.tv_section_header_registros_total.text = Utils.formatCurrency(total)
            
            if (expanded)
                itemView.iv_section_header_registros_expand.setImageResource(R.drawable.ic_arrow_up)
            else
                itemView.iv_section_header_registros_expand.setImageResource(R.drawable.ic_arrow_down)
            
            itemView.setOnClickListener {
                expanded = !expanded
                adapter.notifyDataSetChanged()
            }
        }
    }
    
    private inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        fun bindView(movimento: Movimento) {
            
            itemView.tv_movimento_descricao.text = movimento.descricao
            itemView.tv_movimento_valor.text = Utils.formatCurrency(movimento.valor)
            itemView.tv_movimento_data.text = movimento.data!!.formattedDate()
    
    
            if (!checkableMode) {
                unselectItem(movimento)
            }
            
            itemView.setOnClickListener {
                if (!checkableMode) {
                    DetalhesRegistroDialog(itemView.context, movimento)
                } else {
                    if (!RegistroContext.registrosParaExcluir.contains(movimento)) {
                        selectItem(movimento)
                    } else {
                        unselectItem(movimento)
                    }
                    
                    mOnCheckableModeItemSelectedListener(RegistroContext.registrosParaExcluir.size)
                }
            }
            
            itemView.setOnLongClickListener {
                if (multiSelectModeEnabled) {
                    if (!checkableMode) {
                        selectItem(movimento)
                        checkableMode = true
                        Trigger.launch(Events.HabilitarModoMultiSelecao())
                    }
                }
                
                return@setOnLongClickListener checkableMode
            }
            
        }
    
        private fun selectItem(movimento: Movimento) {
            itemView.setBackgroundResource(R.drawable.background_section_item_selected)
            RegistroContext.registrosParaExcluir.add(movimento)
        }
    
        private fun unselectItem(movimento: Movimento) {
            itemView.setBackgroundResource(android.R.drawable.screen_background_light)
            RegistroContext.registrosParaExcluir.remove(movimento)
        }
    }
    
}
