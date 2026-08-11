import React, { useState } from 'react';
import { CheckSquare, ArrowLeft, Plus, Trash2, CheckCircle2, Circle } from 'lucide-react';

interface TodoItem {
  id: string;
  title: string;
  isCompleted: boolean;
}

interface TodoPageProps {
  onBack: () => void;
}

export const TodoPage: React.FC<TodoPageProps> = ({ onBack }) => {
  const [todos, setTodos] = useState<TodoItem[]>([
    { id: '1', title: 'Cek gaun / jas perform & kebersihan kostum', isCompleted: true },
    { id: '2', title: 'Konfirmasi rundown acara & musik pengiring ke WO', isCompleted: true },
    { id: '3', title: 'Soundcheck & testing microphone di venue', isCompleted: false },
    { id: '4', title: 'Briefing nama pengantin & VIP dengan pihak keluarga', isCompleted: false },
  ]);

  const [newTodo, setNewTodo] = useState('');

  const handleAddTodo = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTodo.trim()) return;
    setTodos([...todos, { id: Date.now().toString(), title: newTodo, isCompleted: false }]);
    setNewTodo('');
  };

  const toggleTodo = (id: string) => {
    setTodos(todos.map(t => t.id === id ? { ...t, isCompleted: !t.isCompleted } : t));
  };

  const deleteTodo = (id: string) => {
    setTodos(todos.filter(t => t.id !== id));
  };

  return (
    <div className="space-y-5 animate-fade-in max-w-4xl mx-auto pb-10">
      <div className="flex items-center gap-3">
        <button
          onClick={onBack}
          className="p-2 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 cursor-pointer"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h2 className="text-xl font-black text-slate-900 dark:text-white tracking-tight flex items-center gap-2">
            <CheckSquare className="w-5 h-5 text-purple-600" />
            <span>Daftar Tugas & To-Do MC</span>
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Checklist persiapan panggung, kostum, soundcheck, dan gladi resik.
          </p>
        </div>
      </div>

      <form onSubmit={handleAddTodo} className="flex gap-2">
        <input
          type="text"
          value={newTodo}
          onChange={(e) => setNewTodo(e.target.value)}
          placeholder="Tambah tugas persiapan baru..."
          className="flex-1 px-4 py-2.5 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 text-xs outline-none focus:border-indigo-600"
        />
        <button
          type="submit"
          className="py-2.5 px-4 rounded-xl bg-indigo-600 text-white font-extrabold text-xs flex items-center gap-1 shadow-md cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Tambah</span>
        </button>
      </form>

      <div className="bg-white dark:bg-slate-800 rounded-3xl border border-slate-200/80 dark:border-slate-700 shadow-sm divide-y divide-slate-100 dark:divide-slate-700/60 overflow-hidden">
        {todos.map((t) => (
          <div key={t.id} className="p-4 flex items-center justify-between gap-3">
            <button
              onClick={() => toggleTodo(t.id)}
              className="flex items-center gap-3 text-left flex-1 cursor-pointer"
            >
              {t.isCompleted ? (
                <CheckCircle2 className="w-5 h-5 text-emerald-500 flex-shrink-0" />
              ) : (
                <Circle className="w-5 h-5 text-slate-300 flex-shrink-0" />
              )}
              <span className={`text-xs font-bold ${t.isCompleted ? 'line-through text-slate-400' : 'text-slate-800 dark:text-slate-200'}`}>
                {t.title}
              </span>
            </button>

            <button
              onClick={() => deleteTodo(t.id)}
              className="p-1.5 text-slate-400 hover:text-rose-500 transition-colors cursor-pointer"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};
