package elevator;

// 输入保证只在单轿厢情况下输入REPAIR类指令，所以无需在轿厢状态包含和REPAIR有关的东西
public enum ShaftState {
    NORMAL,
    UP_ACCEPT,
    UPDATE,
    DOUBLE,
    REC_ACCEPT,
    RECYCLE
}
