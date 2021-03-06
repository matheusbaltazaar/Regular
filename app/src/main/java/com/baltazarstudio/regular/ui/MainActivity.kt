package com.baltazarstudio.regular.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import com.baltazarstudio.regular.R
import com.baltazarstudio.regular.context.MovimentoContext
import com.baltazarstudio.regular.notification.Notification
import com.baltazarstudio.regular.observer.Trigger
import com.baltazarstudio.regular.observer.TriggerEvent
import com.baltazarstudio.regular.ui.backup.DadosBackupActivity
import com.baltazarstudio.regular.ui.entradas.EntradasFragment
import com.baltazarstudio.regular.ui.registros.RegistrosFragment
import com.baltazarstudio.regular.ui.resumo.ResumoFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    
    /**
     *
     * Próximas Atividades do APP
     *
     * - (DONE) SELEÇÃO MÚLTIPLA DOS MOVIMENTOS
     * - IMPLEMENTAÇÃO DE NOTAS DE LEMBRETES
     * - VISUALIZAÇÃO EM GRADE/LISTA DAS NOTAS
     * - LIVE SCROLL ? (IDEIA !!!!!!!)
     * - (DONE) RESUMO DE TODOS OS DADOS + FILTROS VARIADOS
     * - (DONE) FORMA MAIS VIÁVEL/PRÁTICA DE CAPTURAR DATA
     * - (DONE) APONTAR NO POPUP DOS REGISTROS SE PERTENCE A ALGUMA DESPESA
     * - PIN de acesso regular
     * - (DONE) Editar despesa (Perguntar se registros serão alterados também)
     * - (DONE) Nova Fonte
     * - (DONE) Manutenção: Alterar registro pelo "Ver Todos" da aba despesa não altera o popup
     * - (DONE) Despesa: Agregar/desagregar registro existente
     * - (DONE) Despesa: Dia do vencimento
     * - (DONE) Despesa: Quantidade Registros
     * - (DONE) Manutenção: Inverter ordem resultado das pesquisas registros
     * - (DONE) Manutenção: Alterar ou excluir registro enquanto há pesquisa, volta todos registros ao invés da última pesquisa
     * - (DONE) Manutenção: Inverter ordem resultado dos registros "Ver Todos" das despesas
     * - (DONE) Correção: Dropdown dia vencimento não se ajustando a diferente resoluções
     *
     */
    



    private val registrosFragment = RegistrosFragment()
    
    var searchMenuItem: MenuItem? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.setTitle(R.string.activity_title_meus_registros)
        setSupportActionBar(toolbar)
        
        
        val toggle = DrawerToggle(this, drawer_layout, toolbar)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        
        drawer_navigation_view.setNavigationItemSelectedListener(this)
        onNavigationItemSelected(drawer_navigation_view.menu.getItem(0))
        
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_drawer_movimentos -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, registrosFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
            
                toolbar.setTitle(R.string.activity_title_meus_registros)
                searchMenuItem?.isVisible = true
            }
            R.id.menu_drawer_entradas -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, EntradasFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
            
                toolbar.setTitle(R.string.activity_title_entradas)
                searchMenuItem?.isVisible = false
            }
            R.id.menu_drawer_resumo -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ResumoFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
    
                toolbar.setTitle(R.string.activity_title_resumos)
                searchMenuItem?.isVisible = false
            }
        }
    
        item.isChecked = true
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        searchMenuItem = menu.findItem(R.id.action_pesquisar)
        searchMenuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                registrosFragment.desabilitarTabs()
                return true
            }
    
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                registrosFragment.habilitarTabs()
                return true
            }
        })
    
        val searchView = searchMenuItem!!.actionView as SearchView
        searchView.onActionViewCollapsed()
        searchView.queryHint = "Digite sua busca..."
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { return false }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                MovimentoContext.textoPesquisa = newText
                Trigger.launch(TriggerEvent.FiltrarMovimentosPelaDescricao())
                return true
            }
        })
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_dados_backup -> {
                startActivity(intentFor<DadosBackupActivity>())
                if (searchMenuItem?.isVisible!!) {
                    searchMenuItem?.collapseActionView()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun setupFirebaseMessaging() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }
            
                Log.w("FCM Token Registration", task.result)
            }
    }
    
    private fun registerGlobalUIMessage() {
        Trigger.watcher().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe { t ->
                when (t) {
                    is TriggerEvent.Toast -> toast(t.message)
                    is TriggerEvent.Snack -> {
                        Snackbar.make(findViewById(android.R.id.content), t.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }.apply {  }
    }
    
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            val menuItem = drawer_navigation_view.menu.getItem(0)
            if (!menuItem.isChecked) {
                onNavigationItemSelected(menuItem)
                return
            }
            
            super.onBackPressed()
        }
    }
    
    override fun onStart() {
        super.onStart()
    
        setupFirebaseMessaging()
        registerGlobalUIMessage()
    }
    
    override fun onResume() {
        super.onResume()
        Notification.notificar(this)
    }
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }
    
    private class DrawerToggle(
        activity: Activity?,
        private val drawerLayout: DrawerLayout?,
        toolbar: Toolbar?
    ) : ActionBarDrawerToggle(
        activity, drawerLayout, toolbar, android.R.string.yes, android.R.string.cancel
    ) {
        
        override fun onDrawerOpened(drawerView: View) {
            super.onDrawerOpened(drawerView)
            drawerView.bringToFront()
            drawerLayout?.requestLayout()
        }
        
    }
}
