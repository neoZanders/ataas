import { useEffect, useMemo, useState } from "react";
import { GripVertical } from "lucide-react";

export enum SessionType {
    grading = "grading",
    laboration = "laboration",
    help = "help",
    exercise = "exercise",
}

type RankingState = Record<SessionType, number | null>;

const defaultOrder: SessionType[] = [
    SessionType.grading,
    SessionType.laboration,
    SessionType.help,
    SessionType.exercise,
];

interface SessionTypeRankerProps {
    value?: RankingState;
    onChange?: (value: RankingState) => void;
}

function rankingToOrder(ranking?: RankingState): SessionType[] {
    if (!ranking) return defaultOrder;

    const allHaveRanks = defaultOrder.every(
        (sessionType) => ranking[sessionType] !== null && ranking[sessionType] !== undefined
    );

    if (!allHaveRanks) return defaultOrder;

    return [...defaultOrder].sort(
        (a, b) => (ranking[a] ?? 999) - (ranking[b] ?? 999)
    );
}

function orderToRanking(order: SessionType[]): RankingState {
    return {
        [SessionType.grading]: order.indexOf(SessionType.grading) + 1,
        [SessionType.laboration]: order.indexOf(SessionType.laboration) + 1,
        [SessionType.help]: order.indexOf(SessionType.help) + 1,
        [SessionType.exercise]: order.indexOf(SessionType.exercise) + 1,
    };
}

function moveItem<T>(array: T[], fromIndex: number, toIndex: number): T[] {
    const next = [...array];
    const [moved] = next.splice(fromIndex, 1);
    next.splice(toIndex, 0, moved);
    return next;
}

export default function SessionTypeRanker({value, onChange }: SessionTypeRankerProps) {
    const [internalOrder, setInternalOrder] = useState<SessionType[]>(() =>
        rankingToOrder(value)
    );
    const [draggedItem, setDraggedItem] = useState<SessionType | null>(null);
    const [dragOverItem, setDragOverItem] = useState<SessionType | null>(null);

    const currentOrder = useMemo(() => {
        return value ? rankingToOrder(value) : internalOrder;
    }, [value, internalOrder]);

    useEffect(() => {
        if (value) {
            setInternalOrder(rankingToOrder(value));
        }
    }, [value]);

    const updateOrder = (nextOrder: SessionType[]) => {
        if (!value) {
            setInternalOrder(nextOrder);
        }

        onChange?.(orderToRanking(nextOrder));
    };

    const handleDragStart = (sessionType: SessionType) => {
        setDraggedItem(sessionType);
    };

    const handleDragOver = (
        e: React.DragEvent<HTMLDivElement>,
        sessionType: SessionType
    ) => {
        e.preventDefault();
        if (dragOverItem !== sessionType) {
            setDragOverItem(sessionType);
        }
    };

    const handleDrop = (targetSessionType: SessionType) => {
        if (!draggedItem || draggedItem === targetSessionType) {
            setDraggedItem(null);
            setDragOverItem(null);
            return;
        }

        const fromIndex = currentOrder.indexOf(draggedItem);
        const toIndex = currentOrder.indexOf(targetSessionType);

        if (fromIndex === -1 || toIndex === -1) {
            setDraggedItem(null);
            setDragOverItem(null);
            return;
        }

        const nextOrder = moveItem(currentOrder, fromIndex, toIndex);
        updateOrder(nextOrder);

        setDraggedItem(null);
        setDragOverItem(null);
    };

    const handleDragEnd = () => {
        setDraggedItem(null);
        setDragOverItem(null);
    };


    return (
        <div className="w-full rounded-2xl border border-slate-200 bg-white p-6">
            <div className="mb-4">
                <p className="text-sm text-slate-600">
                    Rank session types. Drag to reorder. Rank 1 is most preferred and rank 4 is least preferred.
                </p>
            </div>

            <div className="space-y-3">
                {currentOrder.map((sessionType, index) => {
                    const isDragging = draggedItem === sessionType;
                    const isDraggedOver = dragOverItem === sessionType;

                    return (
                        <div
                            key={sessionType}
                            draggable
                            onDragStart={() => handleDragStart(sessionType)}
                            onDragOver={(e) => handleDragOver(e, sessionType)}
                            onDrop={() => handleDrop(sessionType)}
                            onDragEnd={handleDragEnd}
                            className={[
                                "flex cursor-grab items-center justify-between rounded-2xl border px-4 py-3 transition",
                                isDragging
                                    ? "border-[#003b5c] bg-slate-100 opacity-50"
                                    : "border-slate-200 bg-white",
                                isDraggedOver ? "ring-2 ring-[#003b5c]/20" : "",
                            ].join(" ")}
                        >
                            <div className="flex items-center gap-3">
                                <GripVertical className="h-5 w-5 text-slate-400" />
                                <div>
                                    <p className="capitalize font-medium text-slate-900">
                                        {sessionType}
                                    </p>
                                    <p className="text-xs text-slate-500">
                                        Rank {index + 1}
                                    </p>
                                </div>
                            </div>

                            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[#003b5c] text-sm font-semibold text-white">
                                {index + 1}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}