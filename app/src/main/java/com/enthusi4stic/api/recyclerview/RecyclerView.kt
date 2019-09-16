package com.enthusi4stic.api.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enthusi4stic.api.recyclerview.BindView.Companion.DefaultID
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField

class RecyclerViewAdapter<T : Any>(
    dataClass: KClass<T>,
    private val recyclerView: RecyclerView,
    private val list: List<T>,
    private val context: Context,
    private val xmlLayoutId: Int,
    private val attachToRoot: Boolean,
    private val layoutManager: LayoutManager
) {
    enum class LayoutManager {
        LinearLayoutManager {
            override fun getLayoutManager(context: Context) = LinearLayoutManager(context)
        },
        GridLayoutManager {
            override fun getLayoutManager(context: Context) = GridLayoutManager(
                context,
                androidx.recyclerview.widget.GridLayoutManager.DEFAULT_SPAN_COUNT
            )
        };

        abstract fun getLayoutManager(context: Context): RecyclerView.LayoutManager
    }

    private val views = mutableMapOf<BindView, View>()
    private val fields = mutableMapOf<BindView, Field>()
    private var bindAction: ((View, T, Int) -> Unit)? = null
    private var onItemClickListener: ((T, Int) -> Unit)? = null

    fun setCustomBind(bind: View.(T, Int) -> Unit): RecyclerViewAdapter<T> {
        bindAction = bind
        return this
    }

    fun setOnItemClickListener(action: T.(Int) -> Unit): RecyclerViewAdapter<T> {
        onItemClickListener = action
        return this
    }

    init {
        dataClass.declaredMemberProperties.filter { BindView::class in it.annotations.map { it.annotationClass } }
            .forEach {
                val field = it.javaField!!
                field.isAccessible = true
                fields += it.findAnnotation<BindView>()!! to field
            }
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bindItem(list[position], position)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(context).inflate(xmlLayoutId, parent, attachToRoot)
            for ((bindView, field) in fields) {
                val id = bindView.id
                val resId = itemView.resources.getIdentifier(
                    if (id == DefaultID) field.name else id,
                    "id",
                    context.packageName
                )
                if (resId == 0) {
                    throw ViewNotFoundException(id)
                }
                val view = itemView.findViewById<View>(resId)
                views += bindView to view
            }
            return ViewHolder(itemView)
        }
    }

    fun apply(): Adapter {
        recyclerView.layoutManager = layoutManager.getLayoutManager(context)
        val adapter = Adapter()
        recyclerView.adapter = adapter
        return adapter
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(t: T, position: Int) {
            for ((bindView, field) in fields) {
                val value = field.get(t)
                val view1 = views[bindView]!!
                val (id, view, fieldName) = bindView
                when (fieldName) {
                    BindView.Field.Text -> when (view) {
                        BindView.View.TextView -> {
                            view1 as? TextView ?: throw UnexpectedViewTypeException(
                                TextView::class.simpleName,
                                view1::class.simpleName
                            )
                            value as? CharSequence ?: throw UnexpectedFieldTypeException(
                                CharSequence::class.simpleName,
                                value::class.simpleName
                            )
                            view1.text = value
                        }
                        else -> {
                            throw OperationNotImplementedException()
                        }
                    }
                    BindView.Field.Visibility -> {
                        t as? VisibilityBind ?: throw OperationNotImplementedException(
                            OperationNotImplementedException.Operation.VisibilityBind
                        )
                        view1.visibility = t.getVisibilityState(id, value).constant
                    }
                    BindView.Field.Enabled -> {
                        value as? Boolean ?: throw UnexpectedFieldTypeException(
                            Boolean::class.simpleName,
                            value::class.simpleName
                        )
                        view1.isEnabled = value
                    }
                }
                bindAction?.invoke(itemView, t, position)
                onItemClickListener?.invoke(t, position)
            }
        }
    }
}

fun <T : Any> RecyclerView.bind(
    dataClass: KClass<T>,
    data: List<T>,
    ctx: Context,
    @LayoutRes xmlLayoutId: Int,
    layoutManager: RecyclerViewAdapter.LayoutManager = RecyclerViewAdapter.LayoutManager.LinearLayoutManager
) =
    RecyclerViewAdapter(
        dataClass,
        this,
        data,
        ctx,
        xmlLayoutId,
        false,
        layoutManager
    )