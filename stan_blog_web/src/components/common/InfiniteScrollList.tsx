import { Grid } from '@mui/material';
import { ReactNode, useEffect, isValidElement, cloneElement } from 'react';

export default function InfiniteScrollList<T>(
  props: Readonly<{
    loading: boolean;
    dataSource: Array<T>;
    renderItem: (data: T) => ReactNode;
    renderSkeleton?: () => ReactNode;
    hasMore: boolean;
    loadMore: (filter: any) => void;
    option: any;
  }>
) {
  const { loading, dataSource, renderItem, renderSkeleton, loadMore, hasMore, option } = props;

  useEffect(() => {
    const scrollEvent = () => {
      if (!hasMore || loading) return;
      if (document.documentElement.scrollHeight * 0.8 <= document.documentElement.clientHeight + document.documentElement.scrollTop) {
        loadMore(option);
      }
    };
    window.addEventListener('scroll', scrollEvent);
    return () => {
      window.removeEventListener('scroll', scrollEvent);
    };
  }, [hasMore, loading, option]);

  return (
    <Grid
      container
      sx={{ overFlowY: 'auto' }}
      spacing={1}
    >
      {dataSource.map(data => {
        return renderItem(data);
      })}
      {loading &&
        Array.from({ length: 4 }, (_, index) => {
          const el = renderSkeleton?.();
          return isValidElement(el) ? cloneElement(el, { key: `skeleton-${index}` }) : <span key={`skeleton-${index}`}>{el}</span>;
        })}
    </Grid>
  );
}
