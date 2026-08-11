import React, { useState } from 'react';
import { ArrowLeft, CheckSquare, Plus, Trash2 } from 'lucide-react';
import type { TodoItem } from '../types';
import { useAuth } from '../context/AuthContext';

interface TodoPageProps {
  todos: TodoItem[];
  onSaveTodo: (todo: TodoItem) => Promise<void>;
  onDeleteTodo: (id: string) => Promise<void>;
  onBack: () => void;
}

export const TodoPage: React.FC<TodoPageProps> = ({ todos, onSaveTodo, onDeleteTodo, onBack }) => {
  const [newTask, setNewTask] = useState('');
  const { currentUser } = useAuth();

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTask.trim() || !currentUser?.uid) return;
    
    const newTodo: TodoItem = {
      id: Date.now().toString(),
      ownerId: currentUser.uid,
      title: newTask,
      isCompleted: false,
    };
    await onSaveTodo(newTodo);
    setNewTask('');
  };

  const toggleTodo = async (todo: TodoItem) => {
    await onSaveTodo({ ...todo, isCompleted: !todo.isCompleted });
  };

  const completedCount = todos.filter(t => t.isCompleted).length;

  return (
    <div className="animate-fade-in" style={{maxWidth:'800px', margin:'0 auto', paddingBottom:'24px'}}>
      
      {/* ── HEADER ── */}
      <div className="page-header" style={{alignItems:'center'}}>
        <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
          <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px'}}>
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
              <CheckSquare size={20} color="#7C3AED" />
              Daftar Tugas & To-Do
            </h1>
            <p className="page-subtitle">Checklist persiapan perform dan operasional karier MC.</p>
          </div>
        </div>
      </div>

      <div className="card" style={{padding:'24px', marginBottom:'24px', borderTop:'4px solid #7C3AED'}}>
        <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'16px'}}>
          <div>
            <h3 style={{fontSize:'16px', fontWeight:'700', color:'var(--text-1)'}}>Progress Tugas</h3>
            <p style={{fontSize:'12px', color:'var(--text-3)'}}>{completedCount} dari {todos.length} tugas selesai</p>
          </div>
          <span style={{fontSize:'24px', fontWeight:'800', color:'#7C3AED'}}>
            {todos.length > 0 ? Math.round((completedCount / todos.length) * 100) : 0}%
          </span>
        </div>
        
        {/* Progress Bar */}
        <div style={{width:'100%', height:'8px', background:'var(--bg-surface-2)', borderRadius:'99px', overflow:'hidden'}}>
          <div style={{
            height:'100%', background:'#7C3AED', 
            width: `${todos.length > 0 ? (completedCount / todos.length) * 100 : 0}%`,
            transition: 'width 0.3s ease'
          }} />
        </div>
      </div>

      <form onSubmit={handleAdd} style={{display:'flex', gap:'12px', marginBottom:'24px'}}>
        <input 
          type="text" 
          value={newTask} 
          onChange={e => setNewTask(e.target.value)} 
          placeholder="Tambah tugas baru..." 
          className="input-field" 
          style={{flex:1}}
        />
        <button type="submit" className="btn btn-primary" style={{background:'#7C3AED', borderColor:'#7C3AED', flexShrink:0}}>
          <Plus size={16} /> Tambah
        </button>
      </form>

      <div style={{display:'flex', flexDirection:'column', gap:'12px'}}>
        {todos.length === 0 ? (
          <div className="card empty-state" style={{padding:'40px 24px'}}>
            <CheckSquare size={24} className="empty-state-icon" style={{color:'#7C3AED', background:'#F5F3FF'}} />
            <p style={{fontSize:'14px', fontWeight:'600'}}>Semua tugas selesai!</p>
          </div>
        ) : (
          todos.map(t => (
            <div key={t.id} className="card" style={{padding:'16px', display:'flex', alignItems:'flex-start', gap:'12px', transition:'all 0.2s', opacity: t.isCompleted ? 0.6 : 1}}>
              <div 
                onClick={() => toggleTodo(t)}
                style={{
                  width:'24px', height:'24px', borderRadius:'6px', border:`2px solid ${t.isCompleted ? '#7C3AED' : 'var(--border-strong)'}`,
                  background: t.isCompleted ? '#7C3AED' : 'transparent', display:'flex', alignItems:'center', justifyContent:'center',
                  cursor:'pointer', flexShrink:0, marginTop:'2px', transition:'all 0.15s'
                }}
              >
                {t.isCompleted && <CheckSquare size={14} color="white" />}
              </div>
              
              <div style={{flex:1}} onClick={() => toggleTodo(t)}>
                <p style={{fontSize:'14px', fontWeight:'500', color:'var(--text-1)', textDecoration: t.isCompleted ? 'line-through' : 'none', cursor:'pointer', lineHeight:'1.5'}}>
                  {t.title}
                </p>
              </div>

              <button onClick={() => onDeleteTodo(t.id)} className="btn btn-ghost btn-sm" style={{padding:0, width:'32px', height:'32px', color:'var(--error)', flexShrink:0}}>
                <Trash2 size={16} />
              </button>
            </div>
          ))
        )}
      </div>

    </div>
  );
};
