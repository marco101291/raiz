-- Dolor en la ingle: no siempre viene de L1-L2. Las hernias lumbares bajas lo pueden referir, y hay
-- causas locales (pubalgia, nervios atrapados) que el paciente debe conocer antes de leer "L1" o "L2".
-- Pendiente de revisión clínica antes de publicar (README, hoja de ruta, paso 5).
--
-- Fuentes:
--   [Yukawa 1997]   Yukawa Y, Kato F, Kajino G, et al. Groin pain associated with lower lumbar disc
--                   herniation. Spine. 1997;22(15). PMID 9259784. 4.1% de 512 hernias lumbares bajas
--                   con dolor inguinal, más en L4-L5; vía propuesta: nervio sinuvertebral.
--   [Nakamura 1996] Nakamura SI, Takahashi K, Takahashi Y, et al. The afferent pathways of discogenic
--                   low-back pain: evaluation of L2 spinal nerve infiltration. J Bone Joint Surg Br.
--                   1996;78(4):606-612. El dolor de discos bajos entra sobre todo por la raíz L2.
--   [Bradshaw 1997] Bradshaw C, McCrory P, Bell S, Brukner P. Obturator nerve entrapment: a cause of
--                   groin pain in athletes. Am J Sports Med. 1997;25(3):402-408.

UPDATE nerve_root SET patient_summary =
    'La raíz L1 sale de la parte alta de la columna lumbar. Da sensibilidad a la ingle y ayuda a levantar '
    || 'el muslo. Se afecta con poca frecuencia. El dolor en la ingle tiene muchas otras causas: puede '
    || 'llegar desde discos más bajos de la columna (por ejemplo, L4-L5) o venir de la cadera, de los '
    || 'músculos y la articulación del pubis (pubalgia), de un nervio atrapado en la ingle o de una hernia '
    || 'inguinal. Un profesional debe distinguirlas.'
WHERE code = 'L1';

UPDATE nerve_root SET patient_summary =
    'La raíz L2 sale de la parte alta de la columna lumbar. Da sensibilidad a la ingle y a la parte alta '
    || 'del muslo por delante, y ayuda a levantar el muslo y juntar las piernas. Cuando se irrita puede '
    || 'causar dolor u hormigueo en la ingle y el muslo. Aun así, el dolor en la ingle puede llegar desde '
    || 'discos más bajos de la columna (por ejemplo, L4-L5) o venir de la cadera, de los músculos y la '
    || 'articulación del pubis (pubalgia) o de un nervio atrapado en la ingle. Un profesional debe distinguirlas.'
WHERE code = 'L2';

UPDATE nerve_root SET clinical_notes = clinical_notes
    || ' Las hernias lumbares bajas, sobre todo L4-L5, pueden dar dolor inguinal referido, probablemente '
    || 'por aferencias del nervio sinuvertebral que entran por la raíz L2. Considerar también neuropatías por '
    || 'atrapamiento: obturador (dolor medial del muslo con el ejercicio, desde el origen de los aductores), '
    || 'ilioinguinal, genitofemoral y femorocutáneo lateral.'
WHERE code IN ('L1', 'L2', 'L3');

UPDATE nerve_root SET source_reference = source_reference
    || '; Yukawa 1997 (Spine, PMID 9259784); Nakamura 1996 (J Bone Joint Surg Br 78:606-612); '
    || 'Bradshaw 1997 (Am J Sports Med 25:402-408)'
WHERE code IN ('L1', 'L2', 'L3');
