import { Grid, Link, IconButton } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import DeleteIcon from '@mui/icons-material/Delete';

export const ImagesPanel = (props: { keyPrefix: string; images: { id: number | string; url: string }[]; editable?: boolean; onDelete?: (id: number | string) => void }) =>
  props.images.length > 0 ? (
    <Grid
      container
      sx={{ width: 'calc(100% + 8px)', my: 1 }}
      spacing={1}
    >
      {props.images.map((img, index: number) => (
        <Grid
          item
          xs={12}
          key={`${props.keyPrefix}_img_${index}`}
          sx={{ display: 'flex', justifyContent: 'center' }}
        >
          <div style={{ position: 'relative', width: '100%' }}>
            <Link
              component={RouterLink}
              underline="hover"
              target="_blank"
              to={img.url}
              style={{ display: 'block', width: '100%' }}
            >
              <img
                src={img.url}
                alt={`image-${index}`}
                style={{ width: '100%', height: 'auto', display: 'block', borderRadius: 8 }}
              />
            </Link>
            {props.editable && props.onDelete && (
              <IconButton
                size="small"
                onClick={() => props.onDelete && props.onDelete(img.id)}
                sx={{ position: 'absolute', top: 6, right: 6, backgroundColor: 'rgba(255,255,255,0.92)' }}
              >
                <DeleteIcon fontSize="small" />
              </IconButton>
            )}
          </div>
        </Grid>
      ))}
    </Grid>
  ) : (
    <></>
  );
