/* detri/peel.c --- "Peel off" degenerated (flat) tetrahedra of Trist. */

/*--------------------------------------------------------------------------*/

#include "trist.h"

static void push_if_flat (Basic_istaque_adt q, int ef);

/*--------------------------------------------------------------------------*/

int trist_peel (int hull_ef, int t_flat, int f_hull)
     /* This assumes that the current Trist stores a DT, as generated by
        delaunay(), and that hull_ef is a current hull facet.  It will remove
        all "SoS artefacts" by peeling off all the degenrated tetrahedra
        connected to the CH.  It will return another edfacet on the new hull.
        This assumes that DT is properly 3D; ie, trist_num ().t_proper > 0.
        Obviously, this has an side-effect on the current Trist.
        Time complexity: O(trist_last()).
        NOTE:
        - In general: new hull != CH.
        - If you don't want to specify t_flat or f_hull, set them -1.
        - Ditto for hull_ef (in this case, the return value is 0). */
{
  int cnt = 0, i, t, ef, ff[4];
  Basic_istaque_adt q = basic_istaque_new (If (f_hull < 0, 1, f_hull));
  Assert_always ((hull_ef < 0) or trist_hull_facet (hull_ef));
  print ("> Removing SoS artefacts ...\n");
  trist_for (t)
    if (trist_hull_triangle (t))
      {
        ef = EdFacet (t, 0);
        if (trist_hull_facet (ef))
          ef = Sym (ef);
        push_if_flat (q, ef);
      }
  while (not basic_istaque_empty (q))
    {
      ef = basic_istaque_get (q);
      if (not trist_deleted (TrIndex (ef)))
        { /* remove tetrahedron incident to ef */
          Assert (trist_hull_facet (Sym (ef)));
          cnt ++;
          ff[0] = Sym (ef);
          ff[1] = Fnext (ef);
          ff[2] = Fnext (Enext (ef));
          ff[3] = Fnext (Enext2 (ef));
          upfor (i, 0, 3)
            {
              t = TrIndex (ff[i]);
              if (not trist_deleted (t))
                {
                  if (trist_hull_facet (ff[i]))
                    {
                      trist_hull_facet_set (ff[i], FALSE);
                      trist_delete (ff[i]);
                    }
                  else
                    {
                      trist_hull_facet_set (Sym (ff[i]), TRUE);
                      push_if_flat (q, ff[i]);
                    }
                }
            }
        }
    }
  if (hull_ef < 0)
    hull_ef = 0;
  else
    { /* check if hull_ef is on new hull; otherwise: adjust */
      if (   trist_deleted (TrIndex (hull_ef))
          or (not trist_hull_facet (hull_ef)))
        {
          trist_for (t)
            if (trist_hull_triangle (t))
              {
                hull_ef = EdFacet (t, 0);
                if (not trist_hull_facet (hull_ef))
                  hull_ef = Sym (hull_ef);
                break;
              }
        }
    }
  print ("> Successful: t=t-%d.\n", cnt);
  Assert_always (    ((t_flat < 0) or (cnt == t_flat))
                 and ((hull_ef <= 0) or trist_hull_facet (hull_ef)));
  basic_istaque_dispose (q);
  return (hull_ef);
}

static void push_if_flat (Basic_istaque_adt q, int ef)
     /* Refinement. */
{
  int a, b, c, d;
  trist_triangle (ef, &a, &b, &c);
  d = Dest (Enext (Fnext (ef)));
  if (lia_sign (sos_minor4 (a, b, c, d, 1, 2, 3, 0)) == 0)
    basic_istaque_push (q, ef);
}

/*--------------------------------------------------------------------------*/

/* ...........................................................................
   artefact ('a:rtIfkt), sb. and a. Also arti-. [f. L. arte, abl. of ars
   art + factum, neut.  pa. pple. of facere to make.  (Cf. Sp., Pg. artefacto,
   Ital.  artefatto, adj. and sb.)] A sb.  Anything made by human art and
   workmanship; an artificial product.  In Archol. applied to the rude
   products of aboriginal workmanship as distinguished from natural remains.
   --pat/OED2 server on oed2.cso.uiuc.edu (Oxford English Dictionary)
   ......................................................................... */