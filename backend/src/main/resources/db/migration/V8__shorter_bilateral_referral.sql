-- The result now always shows a "when to go to the ER" section with the emergency red flags, so the
-- bilateral referral messages no longer repeat those signs: the referral banner stays short.
-- Pending clinical review before publishing (README, roadmap, step 5).

UPDATE result_text SET text =
    'Sientes síntomas en la ingle de ambos lados. Suele deberse a causas frecuentes que no son urgentes, '
    || 'como la pubalgia o las caderas, pero conviene una valoración en los próximos días.'
WHERE code = 'referral.BILATERAL_COMMON_CAUSE.LUMBOSACRAL';

UPDATE result_text SET text =
    'Sientes síntomas en los dedos de ambas manos. Suele deberse a causas frecuentes que no son urgentes, '
    || 'como el túnel carpiano, pero conviene una valoración en los próximos días.'
WHERE code = 'referral.BILATERAL_COMMON_CAUSE.CERVICAL';
