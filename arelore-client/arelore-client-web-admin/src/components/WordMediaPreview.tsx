import React from 'react';
import { Button, Image, Space, Typography } from 'antd';
import { SoundOutlined } from '@ant-design/icons';
import { parseExtInfo, resolveDictVoiceUrl, resolveEntryPicture, toHttpsUrl } from '../utils/wordMedia';

type EntryMediaPreviewProps = {
  word?: string;
  extInfo?: string;
};

/** 词条配图 + 英音 / 美音试听 */
export const EntryMediaPreview: React.FC<EntryMediaPreviewProps> = ({ word, extInfo }) => {
  const ext = parseExtInfo(extInfo);
  const picture = resolveEntryPicture(ext);
  const ukUrl = resolveDictVoiceUrl(word || '', 'uk', ext);
  const usUrl = resolveDictVoiceUrl(word || '', 'us', ext);

  const play = (url: string) => {
    if (!url) {
      return;
    }
    try {
      new Audio(url).play();
    } catch {
      // ignore autoplay / network errors
    }
  };

  return (
    <div style={{ marginBottom: 16 }}>
      <Typography.Text strong>媒体预览</Typography.Text>
      <div style={{ marginTop: 8 }}>
        {picture ? (
          <Image
            src={picture}
            alt={word || 'picture'}
            width={160}
            style={{ objectFit: 'cover', borderRadius: 6, background: '#f5f5f5' }}
            fallback="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYwIiBoZWlnaHQ9IjEyMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTYwIiBoZWlnaHQ9IjEyMCIgZmlsbD0iI2VlZSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjOTk5Ij7lm77niYfliqDovb3lpLHotKU8L3RleHQ+PC9zdmc+"
          />
        ) : (
          <Typography.Text type="secondary">暂无配图</Typography.Text>
        )}
      </div>
      <Space style={{ marginTop: 12 }} wrap>
        <Button icon={<SoundOutlined />} disabled={!ukUrl} onClick={() => play(ukUrl)}>英音</Button>
        <Button icon={<SoundOutlined />} disabled={!usUrl} onClick={() => play(usUrl)}>美音</Button>
        {(ext.ukphone || ext.usphone || ext.phone) && (
          <Typography.Text type="secondary">
            {ext.ukphone ? `英 [${ext.ukphone}] ` : ''}
            {ext.usphone ? `美 [${ext.usphone}]` : (!ext.ukphone && ext.phone ? `[${ext.phone}]` : '')}
          </Typography.Text>
        )}
      </Space>
    </div>
  );
};

type BookCoverPreviewProps = {
  cover?: string;
  name?: string;
  size?: number;
};

export const BookCoverPreview: React.FC<BookCoverPreviewProps> = ({ cover, name, size = 48 }) => {
  const src = toHttpsUrl(cover);
  if (!src) {
    return <span style={{ color: '#bbb' }}>-</span>;
  }
  return (
    <Image
      src={src}
      alt={name || 'cover'}
      width={size}
      height={size}
      style={{ objectFit: 'cover', borderRadius: 4 }}
      preview={{ mask: '查看' }}
    />
  );
};
