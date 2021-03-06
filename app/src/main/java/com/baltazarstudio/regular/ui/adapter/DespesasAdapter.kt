package com.baltazarstudio.regular.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.baltazarstudio.regular.R
import com.baltazarstudio.regular.context.DespesaContext
import com.baltazarstudio.regular.context.MovimentoContext
import com.baltazarstudio.regular.model.Despesa
import com.baltazarstudio.regular.observer.Trigger
import com.baltazarstudio.regular.observer.TriggerEvent
import com.baltazarstudio.regular.ui.registros.despesa.CriarDespesaDialog
import com.baltazarstudio.regular.ui.registros.despesa.MovimentosDespesasDialog
import com.baltazarstudio.regular.ui.registros.despesa.RegistrarDespesaDialog
import com.baltazarstudio.regular.util.Utils
import com.baltazarstudio.regular.util.Utils.Companion.formattedDate
import kotlinx.android.synthetic.main.layout_section_item_despesa.view.*
import java.util.*

class DespesasAdapter(context: Context, private val despesas: ArrayList<Despesa>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private val layoutInflater = LayoutInflater.from(context)
    private var expandedItemPosition: Int = -1
    
    override fun getItemCount(): Int {
        return despesas.size
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            layoutInflater.inflate(R.layout.layout_section_item_despesa, parent, false)
        )
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemViewHolder).bindView(position)
    }
    
    private inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(position: Int) {
            val despesa = despesas[position]
            
            itemView.setOnClickListener {
                val lastExpanded = expandedItemPosition
                expandedItemPosition = position
                
                if (lastExpanded == -1) {
                    notifyItemChanged(expandedItemPosition)
                } else if (lastExpanded == position) {
                    expandedItemPosition = -1
                    notifyItemChanged(lastExpanded)
                } else {
                    notifyItemChanged(lastExpanded)
                    notifyItemChanged(expandedItemPosition)
                }
            }
            
            
            if (expandedItemPosition == position) {
                itemView.divider_section_item_despesas.visibility = View.VISIBLE
                itemView.ll_section_item_despesas_acoes.visibility = View.VISIBLE
                itemView.iv_section_item_despesas_expand.setImageResource(R.drawable.ic_arrow_up)
            } else {
                itemView.divider_section_item_despesas.visibility = View.GONE
                itemView.ll_section_item_despesas_acoes.visibility = View.GONE
                itemView.iv_section_item_despesas_expand.setImageResource(R.drawable.ic_arrow_down)
            }
            
            // NOME
            itemView.tv_section_item_despesas_nome.text = despesa.nome
            
            // ULTIMO REGISTRO
            val ultimoRegistro = MovimentoContext.getDAO(itemView.context).getUltimoRegistro(despesa.codigo!!)
            if (ultimoRegistro == 0L) {
                itemView.tv_section_item_despesas_ultimo_registro.text = "Não há registros."
            } else {
                itemView.tv_section_item_despesas_ultimo_registro.text = "Último registro: ${ultimoRegistro.formattedDate()}"
            }
            
            // DIA VENCIMENTO
            if (despesa.diaVencimento != 0) {
                itemView.ll_section_item_despesas_vencimento.visibility = View.VISIBLE
                itemView.tv_section_item_despesas_dia_vencimento.text = despesa.diaVencimento.toString()
            } else {
                itemView.ll_section_item_despesas_vencimento.visibility = View.GONE
            }
    
            // VALOR
            itemView.tv_section_item_despesas_valor.text = Utils.formatCurrency(despesa.valor)
            
            // REGISTRAR
            itemView.button_section_item_despesas_registrar.setOnClickListener {
                val dialog = RegistrarDespesaDialog(itemView.context, despesa)
                dialog.setOnDespesaRegistrada {
                    Trigger.launch(TriggerEvent.UpdateTelaMovimento())
                    notifyItemChanged(position)
                }
                dialog.show()
            }
            
            // TODAS DESPESAS
            itemView.button_section_item_despesas_todos.isEnabled = ultimoRegistro != 0L
            itemView.button_section_item_despesas_todos.setOnClickListener {
                val registrosDaDespesa = MovimentoContext.getDAO(itemView.context).getRegistrosPelaDespesa(despesa.codigo!!)
                val dialog = MovimentosDespesasDialog(itemView.context, registrosDaDespesa)
                dialog.show()
            }
            
            val qtd = "${MovimentoContext.getDAO(itemView.context).getQuantidadeRegistrosPorDespesa(despesa.codigo!!)}"
            itemView.button_section_item_despesas_todos.setText("Ver Todos ($qtd)")
    
            // OPÇOES
            itemView.iv_section_item_despesas_opcoes.setOnClickListener {
                val popupMenu = PopupMenu(itemView.context, it)
                popupMenu.menu.add(Menu.NONE, 0, Menu.NONE, "Editar")
                popupMenu.menu.add(Menu.NONE, 1, Menu.NONE, "Excluir")
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        0 -> editarDespesa(despesa)
                        1 -> excluirDepesa(despesa)
                    }
                    false
                }
                popupMenu.show()
            }
            
        }
    
        private fun editarDespesa(despesa: Despesa) {
            val dialog = CriarDespesaDialog(itemView.context)
            dialog.editar(despesa)
            dialog.show()
        }
    
    
        private fun excluirDepesa(despesa: Despesa) {
            AlertDialog.Builder(itemView.context).setTitle("Excluir")
                .setMessage("Deseja realmente deletar esta despesa?")
                .setPositiveButton("Excluir") { _, _ ->
                    DespesaContext.getDAO(itemView.context).deletar(despesa)
                    Trigger.launch(TriggerEvent.Toast("Removido!"))
                    Trigger.launch(TriggerEvent.UpdateTelaDespesa())
                }.setNegativeButton("Cancelar", null)
                .show()
        }
    
    }
}
