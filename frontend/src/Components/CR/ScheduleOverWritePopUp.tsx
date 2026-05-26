import {X} from "lucide-react";


interface ScheduleOverWritePopUpProps {
    isOpen: boolean;
    onClose: () => void;
    onRunAlgorithm: () => void | Promise<void>;
}

export function ScheduleOverWritePopUpProps({isOpen, onClose, onRunAlgorithm}: ScheduleOverWritePopUpProps){

    if (!isOpen) return null;

    return (

        <div
            className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/30 backdrop-blur-sm px-4"
            onMouseDown={(e) => {
                if (e.target === e.currentTarget) onClose();
            }}
        >
            <div className="relative w-full max-w-2xl rounded-3xl bg-white shadow-xl ring-1 ring-slate-200">
                <button
                    type="button"
                    onClick={onClose}
                    className="absolute right-4 top-4 inline-flex h-9 w-9 items-center justify-center rounded-full text-slate-400 hover:bg-slate-100 hover:text-slate-600 cursor-pointer"
                    aria-label="Close popup"
                >
                    <X className="h-4 w-4" />
                </button>
                <div className="px-6 pt-6 pb-4">
                    <h2 className="text-xl font-bold text-slate-900">Warning</h2>
                    <p className="mt-1 text-lg text-slate-900">
                        Running algorithm when schedule has already been created will overwrite old schedule, are you sure you want to run algorithm?
                    </p>
                </div>

                <div className="px-6 pb-6 space-y-4">
                    <div>
                        <button
                            type="button"
                            onClick={onRunAlgorithm}
                            className="mt-4 rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white hover:bg-[#002741] disabled:opacity-50 cursor-pointer"
                        >
                            Run algorithm
                        </button>

                    </div>
                </div>
            </div>
        </div>
    )
}