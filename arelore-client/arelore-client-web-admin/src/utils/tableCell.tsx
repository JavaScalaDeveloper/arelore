import React from 'react';
import { Tooltip } from 'antd';

/** 表格单元格：单行省略 + hover 展示全文 */
export const EllipsisCell: React.FC<{ text?: unknown; empty?: string }> = ({ text, empty = '-' }) => {
  const value = text == null || text === '' ? empty : String(text);
  const tip = value === empty ? undefined : value;
  return (
    <Tooltip placement="topLeft" title={tip}>
      <span
        style={{
          display: 'block',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap'
        }}
      >
        {value}
      </span>
    </Tooltip>
  );
};

export const ellipsisColumn = (
  title: string,
  dataIndex: string,
  width?: number
) => ({
  title,
  dataIndex,
  width,
  ellipsis: { showTitle: false as const },
  render: (text: unknown) => <EllipsisCell text={text} />
});
