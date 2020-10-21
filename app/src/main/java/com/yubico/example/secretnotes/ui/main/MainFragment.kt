package com.yubico.example.secretnotes.ui.main

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yubico.example.secretnotes.R

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val listAdapter = MyAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        view.findViewById<RecyclerView>(R.id.note_list).apply {
            layoutManager = linearLayoutManager
            adapter = listAdapter
            addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))
        }

        setHasOptionsMenu(true)

        viewModel.noteList.observe(viewLifecycleOwner, {
            listAdapter.setData(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new_note -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, NoteFragment.newInstance(null))
                    .addToBackStack(null)
                    .commit()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
        private var dataSet: List<String> = listOf()

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ) = MyViewHolder(
            (LayoutInflater.from(parent.context)
                .inflate(R.layout.view_list_item, parent, false) as TextView).apply {
                setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, NoteFragment.newInstance(text.toString()))
                        .addToBackStack(null)
                        .commit()
                }
            })

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.textView.text = dataSet[position]
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size

        fun setData(newData: List<String>) {
            dataSet = newData
            notifyDataSetChanged()
        }
    }
}